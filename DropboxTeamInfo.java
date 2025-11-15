import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class DropboxTeamInfo {
    
    private static final String CLIENT_ID = "dgsz6txnmdas9oc";
    private static final String CLIENT_SECRET = "3rc56wkh2hkg68f";
    private static final String REDIRECT_URI = "https://oauth.pstmn.io/v1/callback";
    private static final String AUTH_CODE = "9X2_XE8bRTEAAAAAAAAAJ4KFkiJLTmo0PFfpj8P2qs0";
    
    public static void main(String[] args) {
        try {
            String tokenResponse = fetchAccessTokenResponse();
            String accessToken = parseAccessToken(tokenResponse);
            String teamMembers = listTeamMembers(accessToken);
            
            System.out.println("Team members: " + teamMembers);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static String fetchAccessTokenResponse() throws Exception {
        URL tokenUrl = new URL("https://api.dropboxapi.com/oauth2/token");
        HttpURLConnection conn = (HttpURLConnection) tokenUrl.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        String data = buildTokenRequestData();
        
        try (OutputStream os = conn.getOutputStream()) {
            os.write(data.getBytes());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder tokenResponse = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) tokenResponse.append(line);
        br.close();

        System.out.println("Token response: " + tokenResponse);
        return tokenResponse.toString();
    }
    
    private static String buildTokenRequestData() throws Exception {
        return "code=" + URLEncoder.encode(AUTH_CODE, "UTF-8")
                + "&grant_type=authorization_code"
                + "&client_id=" + URLEncoder.encode(CLIENT_ID, "UTF-8")
                + "&client_secret=" + URLEncoder.encode(CLIENT_SECRET, "UTF-8")
                + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8");
    }
    
    private static String parseAccessToken(String tokenResponse) {
        String accessToken = null;
        int start = tokenResponse.indexOf("\"access_token\":");
        if (start != -1) {
            start = tokenResponse.indexOf("\"", start + 16) + 1;
            int end = tokenResponse.indexOf("\"", start);
            accessToken = tokenResponse.substring(start, end);
        }

        if (accessToken == null) {
            throw new RuntimeException("Could not parse access_token from response.");
        }
        
        System.out.println("Access token: " + accessToken);
        return accessToken;
    }
    
    private static String listTeamMembers(String accessToken) throws Exception {
        URL apiUrl = new URL("https://api.dropboxapi.com/2/team/members/list_v2");
        HttpURLConnection apiConn = (HttpURLConnection) apiUrl.openConnection();
        apiConn.setRequestMethod("POST");
        apiConn.setRequestProperty("Authorization", "Bearer " + accessToken);
        apiConn.setRequestProperty("Content-Type", "application/json");
        apiConn.setDoOutput(true);

        try (OutputStream os = apiConn.getOutputStream()) {
            os.write("{\"limit\":5}".getBytes());
        }

        BufferedReader apiReader = new BufferedReader(new InputStreamReader(apiConn.getInputStream()));
        StringBuilder apiResponse = new StringBuilder();
        String line;
        while ((line = apiReader.readLine()) != null) apiResponse.append(line);
        apiReader.close();

        return apiResponse.toString();
    }
}
