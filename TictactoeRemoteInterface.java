import java.rmi.*;

//An interface can't implement anything, but it can extend other interface.
public interface TictactoeRemoteInterface extends Remote
{
		//public String sayHello() throws RemoteException;
		public void inicializa() throws RemoteException;
		public boolean finPartida() throws RemoteException;
		public boolean tiradaValida(int casilla) throws RemoteException;
		public void realizaTirada(int casilla) throws RemoteException;
		public int ObtenTirada() throws RemoteException;
		public boolean turnoCruces() throws RemoteException;
		public boolean hanGanadoCirculos() throws RemoteException;
		public boolean hanGanadoCruces() throws RemoteException;
		//private boolean empate() throws RemoteException;
		//static void estadoGanador(int pos);


}