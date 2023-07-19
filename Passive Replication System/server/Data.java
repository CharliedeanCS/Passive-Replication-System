import java.io.Serializable;
import java.security.PublicKey;

//A new Class storing all Data shared and used across the Replicas
public class Data implements Serializable {
    int ItemIDs[];
    //Stores values to iterate through
    int j;
    int s;
    int iteration;
    //Stores arrays to store User Information
    AuctionItem Items[];
    int WinningIDs[];
    int WinningItems[];
    int WinningPrice[];
    int Reserveprices[];
    int CurrentUserIds[];
    String CurrentEmails[];
    PublicKey keylistPublicKeys[];
    int AuctionUserIds[];
}
