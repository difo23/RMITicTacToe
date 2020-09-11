// the Naming class (fir doing the remiregistry lookup) is in the java.rmi package
import java.rmi.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

public class TictactoeRemoteCliente extends MouseAdapter
{


    private static TictactoeRemoteInterface service; // objeto que guarda el estado del juego
    private static final String circulosHTML= "<td><img src=circle2.gif>";
    private static final String emptyMoveHTML = "<td><img src=emptyImage.gif>";
	//las cruces empiezan la partida
    private static final String crucesHTML = "<td><img src=cross2.gif>";

    private static final String comienzanCrucesHTML = "<b>Status:</b> Turno de las cruces";
    private static final String circulosGananHTML = "<b>Status:</b> ¡Los circulos ganan!";
    private static final String crucesGananHTML = "<b>Status:</b> ¡Las cruces ganan!";
    private static final String empateHTML = "<b>Status:</b> Empate";
   
    private JEditorPane jep;
    private HTMLDocument htmlDoc;
    private JFrame frame;

    public TictactoeRemoteCliente() {
	new ImageIcon("circle2.gif"); //circulos tiran con circulos
	new ImageIcon("cross2.gif");  //cruces tiran con cruces
	frame = new JFrame("Tres en raya II");
	jep = new JEditorPane();
	jep.setEditable(false);
	jep.setEditorKit(new TicTacToeEditorKit());
	try {
	  jep.setPage(getClass().getResource("tictactoe_board.html"));
	} catch (java.io.IOException ioe) {}
	htmlDoc = (HTMLDocument)jep.getDocument();
	frame.getContentPane().add(new JScrollPane(jep));
	jep.addMouseListener(this);

	frame.pack();
	frame.setSize(new Dimension(600, 800));
	frame.setVisible(true);

    }

    
    /**
     * Se inicializa una nueva partida
     */
    public void newGame() {
	for (int counter = 0; counter < 9; counter++) {
	    toggleBoardElementHTML(getBoardElement(counter), emptyMoveHTML);
	}
	toggleStatus(comienzanCrucesHTML);
			try
			{
				service.inicializa();
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
    }

    /**
     * Se invoca cuando se hace click con el ratón. Se intentara realizar una tirada de acuerdo a la tirada
     */
    public void mouseReleased(MouseEvent me) {
	int position, respuesta;
	try
	{
	if (me.getClickCount() == 1) {
            position = getBoardPosition(me.getX(), me.getY()); 
		//si el juego ya ha termiado o bien la posición elegida es incorrecta u ocupada suena un bip
            if (service.finPartida() || position == -1 || !service.tiradaValida(position)) {
                Toolkit.getDefaultToolkit().beep();
			return;
	    	} else { 
               showMove(position); 					//pinta mi tirada en la pantalla
		   service.realizaTirada(position); 		//actualiza el estado del juego y cambia el turno al servidor
		   checkStatus(); 					//comprueba si se ha acabado la partida 
		   if (!service.finPartida()){			//si no se ha acabado
		     respuesta = service.ObtenTirada();		//espera la tirada elegida por el ordenador
                 showMove(respuesta);				//pinta la tirada en la pantalla
		     service.realizaTirada(respuesta);	      //actualiza el estado del juego y cambia el turno al cliente
		     checkStatus();					//comprueba si se ha acabado la partida
               }						
		}
	}
	}
	catch(Exception ex)
	{
		ex.printStackTrace();
	}
  }

         
    private void showMove(int position){
	// Update the position
		        Element e = getBoardElement(position);
		try{
			if (service.turnoCruces()){
		        toggleBoardElementHTML(e, crucesHTML);
			}else {
		        toggleBoardElementHTML(e, circulosHTML);
			}
		}
		catch(Exception ex)
			{
				ex.printStackTrace();
			}
  }
 
  private void checkStatus(){
     // Check the current status
  	try{
	if (!service.finPartida()){
		return;
	}

	if (service.hanGanadoCirculos())
		toggleStatus(circulosGananHTML);
	else 
		if (service.hanGanadoCruces())
	        toggleStatus(crucesGananHTML);
		else //empate
		  toggleStatus(empateHTML);

		 } catch(Exception ex)
			{
				ex.printStackTrace();
			}
  }

  
    /**
     * Togglets the status of game to show the passed in html string.
     */
    private void toggleStatus(String newHTML) {
	Element e = htmlDoc.getElement("status");
	try {
	    htmlDoc.setInnerHTML(e, newHTML);
	}
	catch (BadLocationException ble) {
	    System.out.println("BLE: " + ble);
	}
	catch (IOException ioe) {
	    System.out.println("IOE: " + ioe);
	}
    }

    /**
     * Returns the element representing position <code>position</code>.
     */
    private Element getBoardElement(int position) {
        return htmlDoc.getElement(Integer.toString(position));
    }

    /**
     * Resets the html contents of the Element <code>e</code> to 
     * the html string <code>html</code>.
     */
    private void toggleBoardElementHTML(Element e, String html) {
	try {
            Object id = e.getAttributes().getAttribute(HTML.Attribute.ID);
            int insertIndex = html.indexOf(">");
            html = html.substring(0, insertIndex) + " id=" + id +
                   html.substring(insertIndex);
	    htmlDoc.setOuterHTML(e, html);
	}
	catch (BadLocationException ble) {
	    System.out.println("BLE: " + ble);
	}
	catch (java.io.IOException ioe) {
	    System.out.println("IOE: " + ioe);
	}
    }

    /**
     * Returns the board position for the element at location <code>x</code>,
     * <code>y</code>. This will return -1 if the passed in location does not
     * represent a spot on the board.
     */
    private int getBoardPosition(int x, int y) {
        // Determine the offset for the passed in x, y location
	Position.Bias bias[] = new Position.Bias[1];
	int offset = jep.getUI().viewToModel(jep, new Point(x, y), bias);
        // A backward bias typically (at least in normal left to right text)
        // indicates an end of line condition. The passed in point was
        // beyond the visible region of the line. In which case the backward
        // bias indicates the location is at the end offset of the character
        // element. Since we will be using getCharacterElement followed by
        // a check of the bounds we subtract one from the offset so that
        // getCharacterElement returns the Element representing the end of
        // line and NOT the next line.
	if (offset > 0 && bias[0] == Position.Bias.Backward) {
	    offset--;
	}

        // Get the character Element at that location, and find the
        // corresponding TD cell.
	Element e = htmlDoc.getCharacterElement(offset);
	while (e != null && e.getAttributes().getAttribute
	       (StyleConstants.NameAttribute) != HTML.Tag.TD) {
	    e = e.getParentElement();
	}

	if (e != null) {
            // Check that the location is really inside the table cell.
            Rectangle bounds;
            try {
                bounds = jep.getUI().modelToView(jep, e.getStartOffset(),
                                                 Position.Bias.Forward);
                bounds = bounds.union(jep.getUI().modelToView
                                      (jep, e.getEndOffset(),
                                       Position.Bias.Backward));

                if (bounds.contains(x, y)) {
                    // found it
                    Object boardLocation = e.getAttributes().getAttribute
                                     (HTML.Attribute.ID);
                    if (boardLocation != null) {
                        try {
                            return Integer.parseInt((String)boardLocation);
                        } catch (NumberFormatException nfe) {
                        }
                    }
                }
            } catch (BadLocationException ble) {
            }
	}
	return -1;
    }


    /**
     * A subclass of HTMLEditorKit that returns a different ViewFactory.
     */
    private class TicTacToeEditorKit extends HTMLEditorKit {
	public ViewFactory getViewFactory() {
	    return new TicTacToeFactory();
	}
    }


    /**
     * A subclass of the HTMLFactory that will create a
     * <code>TicTacToeFormView</code> for <code>INPUT</code> Elements.
     */
    private class TicTacToeFactory extends HTMLEditorKit.HTMLFactory {
	public View create(Element e) {
	    Object o = e.getAttributes().getAttribute
		       (StyleConstants.NameAttribute);
	    if (o == HTML.Tag.INPUT) {
		return new TicTacToeFormView(e);
	    }
	    return super.create(e);
	}
    }


    /**
     * A subclass of <code>FormView</code> that invokes <code>newGame</code>
     * when the action is performed (the user clicks on the button).
     */
    private class TicTacToeFormView extends FormView {
	TicTacToeFormView(Element e) {
	    super(e);
	}

	public void actionPerformed(ActionEvent ae) {
	    newGame();
	}
    }

   // public static void main (String[] args)
	//{
		//new MyRemoteClient().go();
	//}

	public void go()
	{
		try
		{
			//(MyRemote)it comes out of the registry as type Object, so don't forget the cast
			service= (TictactoeRemoteInterface) Naming.lookup("rmi://127.0.0.1/Remote_TicTacToe");
			// you need the IP address or hostname, And the name used to bind/rebind the service
			//String s= service.sayHello();
			
			//it looks just like a regular old method call? (Except it must acknowledge the RemoteException)
			//System.out.println(s);
		} 
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

    
    public static void main(String[] args) 
    {
    	   SwingUtilities.invokeLater
            (
                    new Runnable() 
                    {
                	    public void run() {
                		 new TictactoeRemoteCliente().go();
                	    }
                	}
            );
        	//partida = new Partida(); //se crea una instancia de partida
        	
    }

}