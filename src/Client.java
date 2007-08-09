import java.io.*;
import java.security.*;
import javax.net.ssl.*;

public class Client implements Runnable
{
    private int port;
    private DataInputStream din;
    private DataOutputStream dout;
    private KeyStore clientKeyStore;
    private KeyStore serverKeyStore;
    private SSLContext sslContext;
    private String host, myJksFile, myJksPwd, otherJksFile;
    private char[] passphrase, publicPass;
    static private SecureRandom secureRandom;

    public Client( String host, int port , String myJksFile, String myPwd,
	    String otherJksFile, String otherPwd )
    {
	this.host = host;
	this.port = port;
	this.myJksFile = myJksFile;
	this.passphrase = myPwd.toCharArray();
	this.otherJksFile = otherJksFile;
	this.publicPass = otherPwd.toCharArray();	
	
	connect( host, port );

	new Thread( this ).start();
    }

    private void setupServerKeystore()
	throws GeneralSecurityException, IOException
    {
	serverKeyStore = KeyStore.getInstance( "JKS" );
	serverKeyStore.load( new FileInputStream( otherJksFile ), publicPass );
    }

    private void setupClientKeyStore()
	throws GeneralSecurityException, IOException
    {
	clientKeyStore = KeyStore.getInstance( "JKS" );
	clientKeyStore.load( new FileInputStream( myJksFile ), passphrase );
    }

    private void setupSSLContext() throws GeneralSecurityException, IOException
    {
	TrustManagerFactory tmf = TrustManagerFactory.getInstance( "SunX509" );
	tmf.init( serverKeyStore );

	KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );
	kmf.init( clientKeyStore, passphrase );

	sslContext = SSLContext.getInstance( "TLS" );
	sslContext.init( kmf.getKeyManagers(),
                     tmf.getTrustManagers(),
                     secureRandom );
    }

    private void connect( String host, int port )
    {
	try
	{
	    setupServerKeystore();
	    setupClientKeyStore();
	    setupSSLContext();

	    SSLSocketFactory sf = sslContext.getSocketFactory();
	    SSLSocket socket = (SSLSocket)sf.createSocket( host, port );
	    
	    InputStream in = socket.getInputStream();
	    OutputStream out = socket.getOutputStream();

	    din = new DataInputStream( in );
	    dout = new DataOutputStream( out );
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

    public void run()
    {
	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	String y;
	
	try
	{
	    System.out.println( din.readUTF() );
	    	    
	    while (true)
	    {
		
		dout.writeUTF( br.readLine() );
		
		System.out.println( y = din.readUTF() );
		
		if(y.indexOf('m') >= 0)
		    break;
	    }
	}
	catch( IOException ie )
	{
	    ie.printStackTrace();
	}
    }

    static public void main( String args[] )
    {
	if (args.length != 6)
	{
	    System.err.println( "Usage: java Client [hostname] [port number] [myJksFile] [myPwd] [serverJksFile] [serverPwd]" );
	    System.exit( 1 );
	}

	System.out.println( "Wait while secure random numbers are initialized...." );
	secureRandom = new SecureRandom();
	secureRandom.nextInt();
	System.out.println( "Done." );

	new Client( args[0], Integer.parseInt(args[1]), args[2], args[3], args[4], args[5] );
    }
}
