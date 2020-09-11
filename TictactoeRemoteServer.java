 import java.rmi.*;
import java.rmi.server.*;

public class TictactoeRemoteServer extends UnicastRemoteObject implements TictactoeRemoteInterface
{

	private static boolean secuenciasGanadoras[] = new boolean[1 << 9]; //almacena las secuencias ganadoras
    private static final int DONE = (1 << 9) - 1;	 			//numero máximo de casillas a ocupar
    private static final int OK = 0;
    private static final int WIN = 1;
    private static final int LOSE = 2;
    private static final int STALEMATE = 3;
    private static final int turnoCruces = 0; 	//turno de las cruces
    private static final int turnoCirculos = 1; //turno de las circulos
    private static final int mejoresJugadas[] = {4, 0, 2, 6, 8, 1, 3, 5, 7}; //para que elija la máquina

    private int circulos; 				//memoriza las casillas ocupadas por las circulos hasta el momento
    private int cruces; 				//memoriza las casillas ocupadas por las cruces hasta el momento
    private int turnoActual; 				//indica de quien es el turno
   	
    public TictactoeRemoteServer() throws RemoteException 
	{
		//constructor de mi clase...
		this.inicializa();

	}

    /**
     * Controla que se ha seleccionado una casilla no ocupada.
     */
    public boolean tiradaValida(int casilla) {
	if ((casilla < 0) || (casilla > 8)) { 			//se ha seleccionado una casilla de las posibles
	    return false;
	}
	if (((cruces | circulos) & (1 << casilla)) != 0) { 	//y la casilla no esta ocupada
	    return false;
	}
	return true;
    }

  public boolean hanGanadoCirculos(){
	return secuenciasGanadoras[circulos];
  }

  public boolean hanGanadoCruces(){
	return secuenciasGanadoras[cruces];
  }

  private boolean empate(){
	return ((cruces|circulos) == DONE);
  }

  public boolean finPartida(){
	return (hanGanadoCirculos() || hanGanadoCruces() || empate());
  }  

  public boolean turnoCruces(){
	return (turnoActual == turnoCruces);
  }

  /* Dada una tirada a una casilla, dicha casilla se apunta en la historia de quien realiza la tirada.
   * La historia de las cruces se representa mediante un entero, cuyos 9 primeros bits de menor peso representan las 9 casillas. 
   * Por ejemplo, si las cruces comienzan a jugar, y tiran a la casilla 4, la variable cruces se le asigna el valor 1
   * desplazado 4 veces hacia la izquierda. Este entero en binario tendrá, de izquierda a derecha y de mayor a menor peso,
   * el siguiente contenido : 0...0010000. En decimal será 2^4 = 32.
   * Si posteriormente tira en la casilla 0 (esquina superior izquierda), se hará un OR lógico a nivel de bit con el valor 
   * anterior, tomando cruces el valor 0...0010001, que en decimal será 2^4 + 1. 
   * Este proceso se repetirá tanto en las variables cruces y circulos, hasta que su valor en decimal represente una secuencia
   * ganadora (horizontal, vertical u oblicua).
   */

  public void realizaTirada(int casilla) { //el usuario realiza una tirada

	if (turnoActual == turnoCruces){
		cruces |= 1 << casilla; 	//actualiza la historia de tiradas de las cruces con la tirada actual
		turnoActual = turnoCirculos;
	}else {
		circulos |= 1 << casilla; 	//actualiza la historia de las tiradas de los circulos con la tirada actual
		turnoActual = turnoCruces;
	}
  }

  public int ObtenTirada() { //el ordenador elije una posicion para tirar supuesto que juega con circulos
    int mejorJugada = -1;

      loop:
	for (int i = 0 ; i < 9 ; i++) {
	    int mw = mejoresJugadas[i];
	    if (((circulos & (1 << mw)) == 0) && ((cruces & (1 << mw)) == 0)) {
		int pw =circulos | (1 << mw);
		if (secuenciasGanadoras[pw]) {
		    // circulos ganan!
		    return mw;
		}
		for (int mb = 0 ; mb < 9 ; mb++) {
		    if (((pw & (1 << mb)) == 0) && ((cruces & (1 << mb)) == 0)) {
			int pb = cruces | (1 << mb);
			if (secuenciasGanadoras[pb]) {
			    // cruces ganan, elijamos otra para intentar que no gane
			    continue loop;
			}
		    }
		}
		// Ni las circulos ni las cruces pueden ganar en una sola tirada.
		if (mejorJugada == -1) {
		    mejorJugada = mw;
		}
	    }
	}
	if (mejorJugada != -1) {
	    return mejorJugada;
 	}
 	// No se encuentra ninguna casilla suficientemente buena. Elegimos la primera que encontremos
	for (int i = 0 ; i < 9 ; i++) {
	    int mw = mejoresJugadas[i];
	    if (((circulos & (1 << mw)) == 0) && ((cruces & (1 << mw)) == 0)) {
		return mw;
	    }
	}

	// No hay movientos posibles
	return -1;
}

//el metodo estadoGanador marca todos estados que contienen alguna secuencia ganadora
   static void estadoGanador(int pos) { 
	for (int i = 0 ; i < DONE ; i++) {
	    if ((i & pos) == pos) {
		secuenciasGanadoras[i] = true;
	    }
	}
    }
 
public void inicializa(){
/**
 * Inicializa las posiciones ganadoras en el vector secuenciasGanadoras
 * las posiciones del tablero se numeran del 0 al 8, comenzando por la esquina superior izquierda
**/
     estadoGanador((1 << 0) | (1 << 1) | (1 << 2)); //fila superior 
	estadoGanador((1 << 3) | (1 << 4) | (1 << 5)); //fila central
	estadoGanador((1 << 6) | (1 << 7) | (1 << 8)); //fila inferior
	estadoGanador((1 << 0) | (1 << 3) | (1 << 6)); //columna izquierda
	estadoGanador((1 << 1) | (1 << 4) | (1 << 7)); //columna central
	estadoGanador((1 << 2) | (1 << 5) | (1 << 8)); //columna de la derecha
	estadoGanador((1 << 0) | (1 << 4) | (1 << 8)); //diagonal de izda
	estadoGanador((1 << 2) | (1 << 4) | (1 << 6)); //diagonal derecha
	
	cruces = circulos = 0; 		// memoriza las tiradas de circulos y cruces
	turnoActual = turnoCruces; 	//Comienza el usuario
}

public static void main(String[] args) 
	{
		try
		{
			TictactoeRemoteInterface service = new TictactoeRemoteServer();
			Naming.rebind("Remote_TicTacToe", service);

		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

  
}