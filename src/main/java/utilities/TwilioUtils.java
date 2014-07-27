package utilities;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.MessageFactory;
import com.twilio.sdk.resource.instance.Account;
import com.twilio.sdk.resource.instance.Message;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import java.util.ArrayList;
import java.util.List;

public class TwilioUtils {

    public static final String ACCOUNT_SID = "ACa01551cabe7f107ee747f4e45d5e27cd";
    public static final String AUTH_TOKEN = "c5f9872bc8f711fada6bae7bd09ce774";

    public static void sendSms( String to, String from, String msg )
            throws TwilioRestException {
        TwilioRestClient client = new TwilioRestClient( ACCOUNT_SID, AUTH_TOKEN );
        Account account = client.getAccount();
        MessageFactory messageFactory = account.getMessageFactory();
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add( new BasicNameValuePair( "To", to ));
        params.add( new BasicNameValuePair( "From", from ));
        params.add( new BasicNameValuePair( "Body", msg ));
        Message sms = messageFactory.create( params );
    }
}