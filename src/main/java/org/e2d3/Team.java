package org.e2d3;

import java.io.Serializable;

import javax.persistence.Id;

public class Team implements Serializable {

    @Id public String id; // A-Z
    public String title; // 作品名
    public String e2d3;
    public String comment;

}
