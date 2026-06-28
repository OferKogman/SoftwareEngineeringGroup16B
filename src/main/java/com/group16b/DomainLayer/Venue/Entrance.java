package com.group16b.DomainLayer.Venue;

import java.util.Objects;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

@Embeddable
public class Entrance {
    private String entranceID;
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name="startRow", column=@Column(name="entrance_startRow")),
        @AttributeOverride(name="startColumn", column=@Column(name="entrance_startColumn")),
        @AttributeOverride(name="rowCount", column=@Column(name="entrance_rowCount")),
        @AttributeOverride(name="columnCount", column=@Column(name="entrance_columnCount"))
    })
    private GridRectangle area;  

    public Entrance(String entranceID, GridRectangle area){
        this.entranceID = entranceID;
        this.area = area;
    }
    public Entrance() {
        // Default constructor for JPA
        this.entranceID = null;
    }

    public String getEntranceID() {
        return entranceID;
    }
    
    public GridRectangle getArea() {
        return area;
    }

    public void setArea(GridRectangle area) {
        this.area = area;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Entrance other)) return false;
        return Objects.equals(entranceID, other.entranceID);
    }

    @Override
    public int hashCode() {
        return entranceID != null ? entranceID.hashCode() : 0;
    }
}
