/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal.entities;
import java.io.Serializable;
import jakarta.persistence.*;
/**
 *
 * @author KIET
 */
@Entity
@Table(name="xt_tohop_monthi")
public class ToHopMon implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idtohop;
    @Column(name="matohop")
    private String matohop;
    @Column(name="mon1")
    private String mon1;
    @Column(name="mon2")
    private String mon2;
    @Column(name="mon3")
    private String mon3;
    @Column(name="tentohop")
    private String tentohop;

    public int getIdtohop() {
        return idtohop;
    }

    public void setIdtohop(int idtohop) {
        this.idtohop = idtohop;
    }

    public String getMatohop() {
        return matohop;
    }

    public void setMatohop(String matohop) {
        this.matohop = matohop;
    }

    public String getMon1() {
        return mon1;
    }

    public void setMon1(String mon1) {
        this.mon1 = mon1;
    }

    public String getMon2() {
        return mon2;
    }

    public void setMon2(String mon2) {
        this.mon2 = mon2;
    }

    public String getMon3() {
        return mon3;
    }

    public void setMon3(String mon3) {
        this.mon3 = mon3;
    }

    public String getTentohop() {
        return tentohop;
    }

    public void setTentohop(String tentohop) {
        this.tentohop = tentohop;
    }
    
    
}
