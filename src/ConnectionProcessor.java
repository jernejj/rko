import java.io.*;

import javax.net.ssl.*;
import java.util.StringTokenizer;

public class ConnectionProcessor implements Runnable
{
	private Server server;
	private SSLSocket sslsocket;
	private DataInputStream din;
	private DataOutputStream dout;
	private String name;

	public ConnectionProcessor( Server server, SSLSocket sslsocket ) 
	{
		this.server = server;
		this.sslsocket = sslsocket;

		try
		{
			this.name = sslsocket.getSession().getPeerPrincipal().getName();
		}
		catch (SSLPeerUnverifiedException ex)
		{
			ex.printStackTrace();
			System.exit( 1 );
		}

		new Thread( this ).start();
	}

	private synchronized String processPosting( String s )
	{
		int delta;
		String izdelek;

		/*
		 * dodaj izdelek x kolicine y
		 */
		try{
			if(s.charAt(0) == 'D' && s.charAt(1) == ' ')
			{
				Skladisce tmp = server.najdiSkladisce(name);
				StringTokenizer st = new StringTokenizer(s);

				if(st.countTokens() != 3 || tmp == null)
					return null;

				st.nextToken();
				izdelek = st.nextToken();
				delta = Integer.parseInt(st.nextToken());
				tmp.dodaj(izdelek, delta );

				return "Dodaj " + izdelek + " za " + delta + " enot. Stanje skladisca je:"+tmp.vrniStanje();
			}
			else if(s.charAt(0) == 'Z' && s.charAt(1) == ' ')
			{
				Skladisce tmp = server.najdiSkladisce(name);
				StringTokenizer st = new StringTokenizer(s);

				if(st.countTokens() != 3 || tmp == null)
					return null;

				st.nextToken();
				izdelek = st.nextToken();
				delta = Integer.parseInt(st.nextToken());

				if( tmp.zmanjsaj(izdelek, delta) < 0 )
					return "Ni dovolj zaloge za izdelek "+ izdelek +" . Stanje skladisca je:"+tmp.vrniStanje();

				return "Zmanjsaj " + izdelek +" za " + delta + " enot. Stanje skladisca je:"+tmp.vrniStanje();

			}
			else if(s.charAt(0) == 'K')
				return "Prekinjam...";

			return null;
		}catch(Exception ex){
			ex.printStackTrace();
			return null;
		}
	}


	public void run()
	{
		String response;
		Skladisce tmp;

		try
		{
			InputStream in = sslsocket.getInputStream();
			OutputStream out = sslsocket.getOutputStream();

			din = new DataInputStream( in );
			dout = new DataOutputStream( out );

			if((tmp = server.najdiSkladisce(name)) == null)
				tmp = server.dodajSkladisce(name);

			dout.writeUTF("Pozdravljen(a) " + name + ". Stanje skladisca je:" + tmp.vrniStanje());

			while( true )
			{
				response = processPosting(din.readUTF());
				if( response != null) {
					
					dout.writeUTF( response );
					if(response.matches("Prekinjam...")){
						break;
					}
				}
				else{
					dout.writeUTF( "Napaka! Prekinjam..." );
					break;
				}
			}
			
			try
			{
				sslsocket.close();
			}
			catch( IOException ie2 )
			{
				System.out.println( "Error closing socket "+sslsocket );
			}

			System.out.println( "Closed connection from socket "+sslsocket );
			
		}
		catch( IOException ie )
		{
			try
			{
				sslsocket.close();
			}
			catch( IOException ie2 )
			{
				System.out.println( "Error closing socket "+sslsocket );
			}

			System.out.println( "Closed connection from socket "+sslsocket );
		}
	}
}