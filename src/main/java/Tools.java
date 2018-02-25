import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class Tools {
    public static String rest(String url){
        try {
            Client client = Client.create();
            WebResource webResource = client.resource(url);
            ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);
            if (response.getStatus()>=300)
                throw new RuntimeException("Failed : HTTP error code : "+ response.getStatus());

            if(response.getStatus() ==200){
                String output = response.getEntity(String.class);
                return output;
            } else
                return "";

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
