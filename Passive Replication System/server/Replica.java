//@Charlie Dean
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.PublicKey;
import java.util.ArrayList;




public class Replica extends Server implements Replicas{


    public static String name;
    public static Data data;
    
    //Extends all functions and variables found in Server
    public Replica(String name){ 
      super(name);
      //When a new Replica imerges this function is ran to update the Replica Data
      AddDataToNewReplica();
    }

    public static void main(String[] args){
        try {

            //Creates a Replica with the name decided in the Users args
            name = args[0];
            // Create server object
            Replica obj = new Replica(name);
            // Create remote object stub from server object
            Replicas stub = (Replicas) UnicastRemoteObject.exportObject(obj, 0);
            // Get registry
            Registry registry2 = LocateRegistry.getRegistry("localhost");
            // Bind the remote object's stub in the registry
            registry2.rebind(name, stub);
    
    
            // Informs the User that the servers ready
            System.out.println(args[0]);
            System.err.println("Server ready");
            
            
          } catch (Exception e) {
          }
    }
    //Creates an object Data and fills in the variables inside with the values found in Replica
    public Data Return() throws RemoteException {
      data = new Data();
      data.AuctionUserIds = AuctionUserIds;
      data.CurrentEmails = CurrentEmails;
      data.CurrentUserIds = CurrentUserIds;
      data.ItemIDs = ItemIDs;
      data.Items = Items;
      data.Reserveprices = Reserveprices;
      data.WinningIDs = WinningIDs;
      data.WinningItems = WinningItems;
      data.WinningPrice = WinningPrice;
      data.iteration = iteration;
      data.j = j;
      data.keylistPublicKeys = keylistPublicKeys;
      data.s = s;
      return data;
    }

    //Changes all the values found in Replica with the ones extracted from the Data input
    public Data Place(Data input) throws RemoteException {
      AuctionUserIds = input.AuctionUserIds;
      CurrentEmails = input.CurrentEmails;
      CurrentUserIds = input.CurrentUserIds;
      ItemIDs = input.ItemIDs;
      Items = input.Items;
      Reserveprices = input.Reserveprices;
      WinningIDs = input.WinningIDs;
      WinningItems = input.WinningItems;
      WinningPrice = input.WinningPrice;
      iteration = input.iteration;
      j = input.j;
      keylistPublicKeys = input.keylistPublicKeys;
      s = input.s;
      return input;
    }


    //Runs in the primary Replica and informs the other Replicas the update of information
    public boolean UpdateReplicas(int i) throws RemoteException
    {
      //Creates a ServerList contains the Servers found by the FrontEnd
      ArrayList<Replicas> serverList = FrontEnd.getServerList();

      //Creates a direct server connecton to the primary
      Replicas Primary = serverList.get(i);
      System.out.println("Connected to the Primary");
      int check = 0;

      //Runs a for loop going through all servers in ServerList
        for(Replicas server: serverList)
        {
          try{
            //if the server is the Primary it doesnt change any info
            if (server == Primary){
            }
            //If the Server is not the primary it runs the server.place function using he primarys Return Function.
            else{
              server.Place(Primary.Return());
              System.out.println("Updated Replica :" + server.getPrimaryReplicaID());
            }
          }
          catch (Exception e) {    
            //If an error occurs check gets incremented
            check = check +1;
          }
        }  
        //Checks if all servers couldnt be accessed, if true returns false
        if (check == serverList.size())
        {
          return false;
        }
        else{
          return true;
        }

    }

    @Override
    public int[] test() throws RemoteException {
      return CurrentUserIds;
    }

  //This updates the current Replica with Information found in the other Active Replicas
  public void AddDataToNewReplica()
  {
    ArrayList<Replicas> serverList = FrontEnd.getServerList();

    for(Replicas server: serverList)
    {
      try{
        //uses the servers place function and inputs every server.return
        Place(server.Return());
        break;
      }
      catch (Exception e) {
      }
    }
  }

}
