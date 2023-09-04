package xyz.unpunished.model;

public enum FileType {
    
    SNR(1397641760),
    GIN(1195986464);
   
    int value;
    
    private FileType(int value){
        this.value = value;
    }
    
    public boolean equals(int value){
        return value == this.value;
    }
    
}
