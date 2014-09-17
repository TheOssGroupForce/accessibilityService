package com.example.acService;

import java.io.Serializable;

/**
 * Created by res on 12/9/14.
 */
public class FieldData implements Serializable{

    public String type;
    public String id;

    FieldData(String type, String id){
        this.type = type;
        this.id = id;
    }
}
