package client;

public class Pairs{
    private String l;
    private String r;
    public Pairs(String l, String r){
        this.l = l;
        this.r = r;
    }
    public String getL(){ return l; }
    public String getR(){ return r; }
    public void setL(String l){ this.l = l; }
    public void setR(String r){ this.r = r; }
}