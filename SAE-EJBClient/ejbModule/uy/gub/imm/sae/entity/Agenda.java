/*
 * SAE - Sistema de Agenda Electronica
 * Copyright (C) 2009  IMM - Intendencia Municipal de Montevideo
 *
 * This file is part of SAE.

 * SAE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uy.gub.imm.sae.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@Table( name = "ae_agendas" )
public class Agenda implements Serializable {

	private static final long serialVersionUID = -8141535473333482435L;
	
	private Integer id;
	private String nombre;
	private String descripcion;
	private Date   fechaBaja;
	
	private List<Recurso> recursos;
	private TextoAgenda textoAgenda;
	
	
	public Agenda () {
		recursos = new ArrayList<Recurso>();
	}
	
	/**
	 * Constructor por copia no en profundidad.
	 */
	public Agenda (Agenda a) {
		
		id = a.getId();
		nombre = a.getNombre();
		descripcion = a.getDescripcion();
		fechaBaja = a.getFechaBaja();
		
		textoAgenda = null;

		recursos = new ArrayList<Recurso>();
	}

	@Id
	@GeneratedValue (strategy = GenerationType.SEQUENCE, generator="seq_agenda")
	@SequenceGenerator (name ="seq_agenda", initialValue = 1, sequenceName = "s_ae_agenda", allocationSize=1)
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}

	@Column (nullable = false, length=8)
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	@Column (name = "fecha_baja")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getFechaBaja() {
		return fechaBaja;
	}
	public void setFechaBaja(Date fin) {
		fechaBaja = fin;
	}
	
	@XmlTransient
	@OneToMany (mappedBy = "agenda")
	public List<Recurso> getRecursos() {
		return recursos;
	}
	public void setRecursos(List<Recurso> recursos) {
		this.recursos = recursos;
	}
	
	@OneToOne (mappedBy = "agenda", fetch = FetchType.EAGER, cascade={CascadeType.ALL})
	public TextoAgenda getTextoAgenda() {
		return textoAgenda;
	}

	public void setTextoAgenda(TextoAgenda textoAgenda) {
		this.textoAgenda = textoAgenda;
	}

	@Column (nullable = false, length=50)
	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}


}
