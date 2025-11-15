import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class DropboxTeamInfo {
    public static void main(String[] args) {
        String clientId = "dgsz6txnmdas9oc";
        String clientSecret = "3rc56wkh2hkg68f";
        String redirectUri = "https://oauth.pstmn.io/v1/callback";
        String authCode = "9X2_XE8bRTEAAAAAAAAAJ4KFkiJLTmo0PFfpj8P2qs0";

        try {
            URL tokenUrl = new URL("https://api.dropboxapi.com/oauth2/token");
            HttpURLConnection conn = (HttpURLConnection) tokenUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String data = "code=" + URLEncoder.encode(authCode, "UTF-8")
                        + "&grant_type=authorization_code"
                        + "&client_id=" + URLEncoder.encode(clientId, "UTF-8")
                        + "&client_secret=" + URLEncoder.encode(clientSecret, "UTF-8")
                        + "&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(data.getBytes());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder tokenResponse = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) tokenResponse.append(line);
            br.close();

            System.out.println("Token response: " + tokenResponse);

            String tokenStr = tokenResponse.toString();
            String accessToken = null;
            int start = tokenStr.indexOf("\"access_token\":");
            if (start != -1) {
                start = tokenStr.indexOf("\"", start + 16) + 1;
                int end = tokenStr.indexOf("\"", start);
                accessToken = tokenStr.substring(start, end);
            }

            if (accessToken == null) {
                throw new RuntimeException("Could not parse access_token from response.");
            }
            System.out.println("Access token: " + accessToken);

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
            while ((line = apiReader.readLine()) != null) apiResponse.append(line);
            apiReader.close();

            System.out.println("Team members: " + apiResponse);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
