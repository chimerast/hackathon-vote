package org.e2d3;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Id;

public class User implements Serializable {

    @Id public String id;
    public String teamId;

    public List<Rating> ratings;

}
