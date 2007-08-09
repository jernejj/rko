import java.io.*;
import java.security.*;
import javax.net.ssl.*;

public class Server implements Runnable
{
	private int port;
	private KeyStore clientKeyStore;
	private KeyStore serverKeyStore;
	private SSLContext sslContext;
	private char[] passphrase, publicPass;
	private String otherJksFile, myJksFile;
	static private SecureRandom secureRandom;
	private String[] cs = {"TLS_RSA_WITH_AES_128_CBC_SHA"};
	private Skladisce skladisca = null, last = null;
	
	public Server( int port, String myJksFile, String myPwd,
			String otherJksFile, String otherPwd )
	{
		this.port = port;
		this.myJksFile = myJksFile;
		this.passphrase = myPwd.toCharArray();
		this.otherJksFile = otherJksFile;
		this.publicPass = otherPwd.toCharArray();	
		
		new Thread( this ).start();
	}
	
	private void setupClientKeyStore()
	throws GeneralSecurityException, IOException
	{
		clientKeyStore = KeyStore.getInstance( "JKS" );
		clientKeyStore.load( new FileInputStream( otherJksFile ), publicPass );
	}
	
	private void setupServerKeystore()
	throws GeneralSecurityException, IOException
	{
		serverKeyStore = KeyStore.getInstance( "JKS" );
		serverKeyStore.load( new FileInputStream( myJksFile ), passphrase );
	}
	
	private void setupSSLContext()
	throws GeneralSecurityException, IOException
	{
		TrustManagerFactory tmf = TrustManagerFactory.getInstance( "SunX509" );
		tmf.init( clientKeyStore );
		
		KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );
		kmf.init( serverKeyStore, passphrase );
		
		sslContext = SSLContext.getInstance( "TLS" );
		sslContext.init( kmf.getKeyManagers(),
				tmf.getTrustManagers(),
				secureRandom );
	}
	
	public void run()
	{
		try
		{
			setupClientKeyStore();
			setupServerKeystore();
			setupSSLContext();
			
			SSLServerSocketFactory sf = sslContext.getServerSocketFactory();
			SSLServerSocket ss = (SSLServerSocket)sf.createServerSocket( port );
			
			// Specify allowed ciphers
			ss.setEnabledCipherSuites(cs);
			
			// Require client authorization
			ss.setNeedClientAuth( true );
			
			System.out.println( "Listening on port "+port+"..." );
			
			while (true)
			{
				SSLSocket sslsocket = (SSLSocket) ss.accept();
				System.out.println( "Got connection from "+sslsocket );
				
				ConnectionProcessor cp = new ConnectionProcessor( this, sslsocket );
			}
		}
		catch( GeneralSecurityException gse )
		{
			gse.printStackTrace();
		}
		catch( IOException ie )
		{
			ie.printStackTrace();
		}
	}
	
	public synchronized Skladisce dodajSkladisce(String name)
	{
		if(skladisca != null)
		{
			last.next = new Skladisce(name);
			last = last.next;
		}
		else
		{
			skladisca = new Skladisce(name);
			last = skladisca;
		}
		
		return last;
	}
	
	public synchronized Skladisce najdiSkladisce(String name)
	{
		Skladisce tmp = skladisca;
		
		while(tmp != null && !tmp.matches(name))
			tmp = tmp.next;
		
		return tmp;
	}
	
	static public void main( String args[] )
	{
		if (args.length != 5)
		{
			System.err.println( "Usage: java Server [port number] [serverJksFile] [serverPwd] [otherJksFile] [otherPwd]" );
			System.exit( 1 );
		}
		
		System.out.println( "Wait while secure random numbers are initialized...." );
		secureRandom = new SecureRandom();
		secureRandom.nextInt();
		System.out.println( "Done." );
		
		new Server( Integer.parseInt(args[0]), args[1], args[2], args[3], args[4] );
	}
}
