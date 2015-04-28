package com.asiainfo.hlog.client.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by c on 2015/4/10.
 */
public class HLogConfigRule {

    private String path ;

    private String level ;

    private List<String> captureWeaves ;


    public HLogConfigRule(String path){
        this(path,null);
    }
    public HLogConfigRule(String path, List<String> captureWeaves) {
        this(path,"none",captureWeaves);
    }
    public HLogConfigRule(String path, String level, List<String> captureWeaves) {
        this.path = path;
        this.level = level;
        this.captureWeaves = captureWeaves;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public List<String> getCaptureWeaves() {
        return captureWeaves;
    }

    public void setCaptureWeaves(List<String> captureWeaves) {
        this.captureWeaves = captureWeaves;
    }

    public List<String> addCaptureWeave(String weave){
        if(this.captureWeaves == null){
            this.captureWeaves = new ArrayList<String>(10);
        }
        if(!this.captureWeaves.contains(weave)){
            this.captureWeaves.add(weave);
        }
        return this.captureWeaves;
    }
}
