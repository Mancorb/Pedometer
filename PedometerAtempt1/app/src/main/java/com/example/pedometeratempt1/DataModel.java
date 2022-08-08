package com.example.pedometeratempt1;

public class DataModel {
    private int id;
    private String fecha;
    private String hora;
    private int pasos;

    //Constructor
    public DataModel (int id, String fecha, String hora, int pasos){
        this.id=id;
        this.fecha=fecha;
        this.hora=hora;
        this.pasos=pasos;
    }

    public DataModel(){
    }

    //toString
    @Override
    public String toString() {
        return "DataModel{" +
                "id=" + id +
                ", fecha='" + fecha + '\'' +
                ", hora='" + hora + '\'' +
                ", pasos=" + pasos +
                '}';
    }



    //GETTERS & SETTERS
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public int getPasos() {
        return pasos;
    }

    public void setPasos(int pasos) {
        this.pasos = pasos;
    }

}
