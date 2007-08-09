import java.util.Enumeration;
import java.util.Hashtable;



public class Skladisce
{
    private String name;
    private  Hashtable<String, Integer> izdelki;
    public Skladisce next = null;
    
    public Skladisce( String name )
    {
    	this.name = name;
    	izdelki = new Hashtable<String, Integer>();
    }
    /*
     *  vrne string v katerem so izdeleki ter njihova kolicina.
     *  format pa je " izdelek1 kolicina1, izdelek2 kolicina2."
     **/
    
    public String vrniStanje()
    {
    	String tmp;
    	StringBuffer stanje = new StringBuffer();
    	
    	for(Enumeration<String> en = izdelki.keys(); en.hasMoreElements(); ){
    		tmp = en.nextElement();
    		stanje.append(" "+tmp);
    		stanje.append(" "+izdelki.get(tmp)+",");
    	}
    	
    	if(stanje.length() == 0)
    		return ".";
    	
    	stanje.replace(stanje.length()-1, stanje.length(), ".");	
    	
    	return stanje.toString();
	
    }
    /*
     * vrne stanje dolocenega izdelka
     * vrnjena vrednsot je tipa int
     **/
    
    public int vrniStanje(String izdelek)
    {
    	if(izdelki.containsKey(izdelek))
    		return izdelki.get(izdelek);
    	else
    		return -1;
    }
    
    public void dodaj(String izdelek, int kolicina )
    {
    	if(izdelki.containsKey(izdelek))
    		izdelki.put(izdelek, izdelki.get(izdelek)+kolicina);
    	else
    		izdelki.put(izdelek, kolicina);
    	
    }
    
    public int zmanjsaj( String izdelek, int kolicina )
    {
    	int zaloga = 0;
    	boolean obstaja = izdelki.containsKey(izdelek);
    	
    	if(obstaja){
    		 zaloga = izdelki.get(izdelek).intValue();
    	}
    	
    	zaloga = zaloga - kolicina;
    	
    	if( zaloga < 0)
    		return -1;
    	
    	if(obstaja){
    		izdelki.put(izdelek, new Integer(zaloga) );
    		return zaloga;
    	}
    	
    	return -1;
    		
    	
    }

    public boolean matches(String name)
    {
	return name.matches(this.name);
    }
}
