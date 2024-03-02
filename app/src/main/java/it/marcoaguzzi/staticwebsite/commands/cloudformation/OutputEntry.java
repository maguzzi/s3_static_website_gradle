package it.marcoaguzzi.staticwebsite.commands.cloudformation;

public class OutputEntry {
    private String key;
    private String value;
    private String exportName;

    public OutputEntry(String key,String value,String exportName) {
        this.key = key;
        this.exportName = exportName;
        this.value = value;   
    }

    public OutputEntry(String key,String value) {
        this(key,value,"-");
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return value;
    }

    public String getExportName() {
        return exportName;
    }
}