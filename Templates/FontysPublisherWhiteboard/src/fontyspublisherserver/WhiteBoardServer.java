/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fontyspublisherserver;

import fontyspublisher.RemotePublisher;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 *
 * @author Nico Kuijpers
 */
public class WhiteBoardServer {

    private static int portNumber = 1099;
    private static String bindingName = "publisher";
    
    /**
     * @param args the command line arguments
     * @throws java.rmi.RemoteException
     */
    public static void main(String[] args) throws RemoteException {
        // Create an instance of RemotePublisher
        RemotePublisher remotePublisher = new RemotePublisher();
        
        // Create registry and register remote publisher
        Registry registry = LocateRegistry.createRegistry(portNumber);
        registry.rebind(bindingName, remotePublisher);
        
        // Remote publisher registered
        System.out.println("Remote publisher registered.");
        System.out.println("Port number  : " + portNumber);
        System.out.println("Binding name : " + bindingName);
    }
}
