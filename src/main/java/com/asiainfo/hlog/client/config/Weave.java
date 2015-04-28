package com.asiainfo.hlog.client.config;

/**
 * Created by c on 2015/4/10.
 */
public class Weave {
    private String name;

    private boolean enable = true;

    public Weave(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public boolean isEnable() {
        return enable;
    }
    public void setEnable(boolean enable) {
        this.enable = enable;
    }


    public boolean isEnable(String name){
        return this.enable && this.name.equals(name);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Weave)) return false;

        Weave weave = (Weave) o;

        if (name != null ? !name.equals(weave.name) : weave.name != null) return false;

        return true;
    }


    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
