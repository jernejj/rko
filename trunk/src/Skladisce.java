import java.util.Enumeration;
import java.util.Hashtable;



public class Skladisce
{
    private String name;
    private Hashtable<String, Integer> izdelki;
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
    	return izdelki.get(izdelek);
    }
    
    public void dodaj(String izdelek, int kolicina )
    {
    	if(izdelki.containsKey(izdelek))
    		izdelki.put(izdelek, izdelki.get(izdelek)+kolicina);
    	else
    		izdelki.put(izdelek, kolicina);
    	
    }
    
    public void zmanjsaj( String izdelek, int kolicina )
    {
    	if(izdelki.containsKey(izdelek))
    		izdelki.put(izdelek, (izdelki.get(izdelek)-kolicina));
    	else
    		izdelki.put(izdelek, kolicina);
    	
    }

    public boolean matches(String name)
    {
	return name.matches(this.name);
    }
}
