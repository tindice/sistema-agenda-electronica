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

package uy.gub.imm.sae.web.mbean.administracion;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.faces.event.ActionEvent;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import org.richfaces.component.UIDataTable;

import uy.gub.imm.sae.business.ejb.facade.AgendarReservas;
import uy.gub.imm.sae.business.ejb.facade.Disponibilidades;
import uy.gub.imm.sae.business.ejb.facade.Recursos;
import uy.gub.imm.sae.common.DisponibilidadReserva;
import uy.gub.imm.sae.common.Utiles;
import uy.gub.imm.sae.common.VentanaDeTiempo;
import uy.gub.imm.sae.web.common.BaseMBean;
import uy.gub.imm.sae.web.common.CupoPorDia;
import uy.gub.imm.sae.web.common.Row;
import uy.gub.imm.sae.web.common.RowList;


public class GenDisponibilidadMBean extends BaseMBean {
	public static final String MSG_ID = "pantalla";
	
	@EJB(name="ejb/RecursosBean")
	private Recursos recursosEJB;

	@EJB(name="ejb/AgendarReservasBean")
	private AgendarReservas agendarReservasEJB;
	
	@EJB(name="ejb/DisponibilidadesBean")
	private Disponibilidades disponibilidadesEJB;

	private SessionMBean sessionMBean;
	private GenDispSessionMBean genDispSessionMBean;
	private DisponibilidadReserva dispAux = new DisponibilidadReserva();
	
	//Tabla asociada a tabla en pantalla para poder saber en que día se posiciona. 
	private UIDataTable cuposDataTable;
//	private List<CupoPorDia> cuposPorDia = new ArrayList<CupoPorDia>();
	
	//Tabla asociada a tabla en pantalla para poder saber en que disponibilidad se posiciona. 
	//private UIDataTable disponibilidadesDataTable;
	
	private UIDataTable tablaDispMatutina;
	private UIDataTable tablaDispVespertina;

	private UIDataTable tablaDispMatutinaModif;
	private UIDataTable tablaDispVespertinaModif;

	
	@PostConstruct
	public void initGenDisponibilidad(){
		//Se controla que se haya Marcado una agenda para trabajar con los recursos
		if (sessionMBean.getAgendaMarcada() == null){
			addErrorMessage("Debe tener una agenda seleccionada", MSG_ID);
		}
		if (sessionMBean.getRecursoMarcado() == null){
			addErrorMessage("Debe tener un recurso seleccionado", MSG_ID);
		}
		
	}

	public DisponibilidadReserva getDispAux() {
		return dispAux;
	}

	public void setDispAux(DisponibilidadReserva dispAux) {
		this.dispAux = dispAux;
	}

	public SessionMBean getSessionMBean() {
		return sessionMBean;
	}
	
	public void setSessionMBean(SessionMBean sessionMBean) {
		this.sessionMBean = sessionMBean;
	}


	public UIDataTable getCuposDataTable() {
		return cuposDataTable;
	}

	public void setCuposDataTable(UIDataTable cuposDataTable) {
		this.cuposDataTable = cuposDataTable;
	}


	public void obtenerCuposModelo(ActionEvent e){
		Boolean huboError = false;
		
		if (sessionMBean.getAgendaMarcada() == null){
			addErrorMessage("Debe tener una agenda seleccionada", MSG_ID);
			huboError = true;
		}
		if (sessionMBean.getRecursoMarcado() == null){
			addErrorMessage("Debe tener un recurso seleccionado", MSG_ID);
			huboError = true;
		}
		if (genDispSessionMBean.getFechaModelo() == null ) {
			addErrorMessage("La fecha no puede ser nula", MSG_ID);
			huboError = true;
		}

		VentanaDeTiempo v = new VentanaDeTiempo();
		//TODO si no se ingresó una fecha, buscar la mayor fecha con cupos.
		if (!huboError ) {
			//Se setea hora 00:00:00
			v.setFechaInicial(Utiles.time2InicioDelDia(genDispSessionMBean.getFechaModelo()) );
			v.setFechaFinal(Utiles.time2FinDelDia(genDispSessionMBean.getFechaModelo()));
			genDispSessionMBean.setDisponibilidadesDelDiaMatutinaModif(null);
			genDispSessionMBean.setDisponibilidadesDelDiaVespertinaModif(null) ;
			//obtenerCupos(v);
			configurarDisponibilidadesDelDiaModif();
		}
	}

	
	public RowList<CupoPorDia> obtenerCupos(VentanaDeTiempo v){
		RowList<CupoPorDia> cuposAux = null;
		try{
			if (sessionMBean.getRecursoMarcado() != null){
				if (v.getFechaInicial() != null && v.getFechaFinal() != null &&
						v.getFechaInicial().compareTo(v.getFechaFinal()) <= 0 ) {

					List<Integer> cupos = agendarReservasEJB.obtenerCuposPorDia(sessionMBean.getRecursoMarcado(), v);
//					List<DisponibilidadReserva> cupos = disponibilidadesEJB.obtenerDisponibilidadesReservas(sessionMBean.getRecursoMarcado(), v);
				
					Calendar fecha = Calendar.getInstance();
					fecha.setTime(v.getFechaInicial());
					Calendar fechaFin = Calendar.getInstance();
					fechaFin.setTime(v.getFechaFinal());
					Integer i = 0;
					List<CupoPorDia> lista_cupos = new ArrayList<CupoPorDia>();
					while ( !fecha.after( fechaFin ) ){
						if (cupos.get(i) != -1){			
							CupoPorDia cupoPorDia = new CupoPorDia();
							cupoPorDia.setDia(fecha.getTime());
							cupoPorDia.setCupoDisponible(cupos.get(i));
							lista_cupos.add(cupoPorDia);
						}
						i++;
						fecha.add(Calendar.DAY_OF_MONTH, 1);
					}

//					dispSessionMBean.setCuposPorDia(new RowList<CupoPorDia>(lista_cupos));
					cuposAux= new RowList<CupoPorDia>(lista_cupos);
				
				}
				else{
					if (v.getFechaInicial().compareTo(v.getFechaFinal()) > 0){
						addErrorMessage("La fecha Hasta debe ser mayor o igual a la fecha Desde", MSG_ID);
					}
					else
					addErrorMessage("Las fechas Desde y/o Hasta no pueden ser nulas", MSG_ID);
				}
			}
			else{
				addErrorMessage("Debe seleccionar una Agenda y Recurso", MSG_ID);
			}
				
		}catch (Exception ex) {
				addErrorMessage(ex, MSG_ID);
		}
		return cuposAux;
	}

	/*
	public String consultarDisponibilidades() {
        //Se busca posición que se quiere consultar
		try {
		CupoPorDia c = ((Row<CupoPorDia>) this.getCuposDataTable().getRowData()).getData();
		if (c != null) {
	        //La siguiente línea no está desplegando el recurso
			sessionMBean.setCupoPorDiaSeleccionado(c);
			dispSessionMBean.setFechaActual(c.getDia());
			//Se configura para que se despliegue en la primer página.
			dispSessionMBean.setPagDisp(1);
			VentanaDeTiempo v = new VentanaDeTiempo();
			v.setFechaInicial(c.getDia());
			v.setFechaFinal(c.getDia());

			List<Disponibilidad> disp = agendarReservasEJB.obtenerDisponibilidades(sessionMBean.getRecursoMarcado(), v);
			
			dispSessionMBean.setDisponibilidades(disp);

			return "consultarPorDia";
		}
		else {
			//sessionMBean.setCupoPorDiaSeleccionado(null);
			addErrorMessage("Debe seleccionar un dia", MSG_ID);
			return null;
		}
		}catch (Exception ex) {
				addErrorMessage(ex, MSG_ID);
				return null;
		}
	}
	*/

	/* Se obtienen las disponibilidades para desplegar en pantalla.*/
	/*
	public void obtenerDisponibilidades(ActionEvent e){
		VentanaDeTiempo v = new VentanaDeTiempo();
		try{
			if (dispSessionMBean.getFechaDesde() != null) {
				v.setFechaInicial(dispSessionMBean.getFechaDesde());
				v.setFechaFinal(dispSessionMBean.getFechaHasta());

				List<Disponibilidad> disp = agendarReservasEJB.obtenerDisponibilidades(sessionMBean.getRecursoMarcado(), v);
				
				dispSessionMBean.setDisponibilidades(disp);
			}
			else{
				addErrorMessage("La fecha Desde no puede ser nula", MSG_ID);
			}
				
		}catch (Exception ex) {
				addErrorMessage(ex, MSG_ID);
		}
	}
*/
	/*
	public DispSessionMBean getDispSessionMBean() {
		return dispSessionMBean;
	}

	public void setDispSessionMBean(DispSessionMBean dispSessionMBean) {
		this.dispSessionMBean = dispSessionMBean;
	}
*/
	/*
	----------------------------------------------------------------------------
	A partir de acá estoy probando disponibilidades en la mañana y la tarde
	*/
	
	/*

	public RowList<DisponibilidadReserva> getDisponibilidadesMatutina() {
		return dispSessionMBean.getDisponibilidadesDelDiaMatutina();
	}

	public RowList<DisponibilidadReserva> getDisponibilidadesVespertina() {
		return dispSessionMBean.getDisponibilidadesDelDiaVespertina();
	}
*/
	public UIDataTable getTablaDispMatutina() {
		return tablaDispMatutina;
	}

	public void setTablaDispMatutina(UIDataTable tablaDispMatutina) {
		this.tablaDispMatutina = tablaDispMatutina;
	}

	public UIDataTable getTablaDispVespertina() {
		return tablaDispVespertina;
	}

	public void setTablaDispVespertina(UIDataTable tablaDispVespertina) {
		this.tablaDispVespertina = tablaDispVespertina;
	}

	/*
	public String configurarDisponibilidadesDelDia() {

		List<DisponibilidadReserva> dispMatutinas   = new ArrayList<DisponibilidadReserva>();
		List<DisponibilidadReserva> dispVespertinas = new ArrayList<DisponibilidadReserva>();

		CupoPorDia c = ((Row<CupoPorDia>) this.getCuposDataTable().getRowData()).getData();
		if (c != null) {
	        //La siguiente línea no está desplegando el recurso
			//sessionMBean.setCupoPorDiaSeleccionado(c);
			dispSessionMBean.setFechaActual(c.getDia());
			//Se configura para que se despliegue en la primer página.
			
			VentanaDeTiempo ventana = new VentanaDeTiempo();
			ventana.setFechaInicial(Utiles.time2InicioDelDia(dispSessionMBean.getFechaActual()));
			ventana.setFechaFinal(Utiles.time2FinDelDia(dispSessionMBean.getFechaActual()));
			
			try {
				List<DisponibilidadReserva> lista = disponibilidadesEJB.obtenerDisponibilidadesReservas(sessionMBean.getRecursoMarcado(), ventana);
				
				for (DisponibilidadReserva d : lista) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(d.getHoraInicio());
					
					if (cal.get(Calendar.AM_PM) == Calendar.AM) {
						//Matutino
						dispMatutinas.add(d);
					}
					else {
						//Vespertino
						dispVespertinas.add(d);
					}
				}
				
				
			} catch (Exception e) { 
				addErrorMessage(e);
				return null;
			}
		}	
		else {
			return null;
		}

		dispSessionMBean.setDisponibilidadesDelDiaMatutina(new RowList<DisponibilidadReserva>(dispMatutinas));
		dispSessionMBean.setDisponibilidadesDelDiaVespertina(new RowList<DisponibilidadReserva>(dispVespertinas));
		return "consultarPorDia";
	}

*/
	public void configurarDisponibilidadesDelDiaModif() {

		List<DisponibilidadReserva> dispMatutinas   = new ArrayList<DisponibilidadReserva>();
		List<DisponibilidadReserva> dispVespertinas = new ArrayList<DisponibilidadReserva>();

		VentanaDeTiempo ventana = new VentanaDeTiempo();
		ventana.setFechaInicial(Utiles.time2InicioDelDia(genDispSessionMBean.getFechaModelo()));
		ventana.setFechaFinal(Utiles.time2FinDelDia(genDispSessionMBean.getFechaModelo()));
			
		try {
			List<DisponibilidadReserva> lista = disponibilidadesEJB.obtenerDisponibilidadesReservas(sessionMBean.getRecursoMarcado(), ventana);

			for (DisponibilidadReserva d : lista) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(d.getHoraInicio());
				
				if (cal.get(Calendar.AM_PM) == Calendar.AM) {
					//Matutino
					dispMatutinas.add(d);
				}
				else {
					//Vespertino
					dispVespertinas.add(d);
				}
			}
			
		} catch (Exception e) { 
				addErrorMessage(e);
		}

		genDispSessionMBean.setDisponibilidadesDelDiaMatutinaModif(new RowList<DisponibilidadReserva>(dispMatutinas));
		genDispSessionMBean.setDisponibilidadesDelDiaVespertinaModif(new RowList<DisponibilidadReserva>(dispVespertinas));
	}

	public UIDataTable getTablaDispMatutinaModif() {
		return tablaDispMatutinaModif;
	}

	public void setTablaDispMatutinaModif(UIDataTable tablaDispMatutinaModif) {
		this.tablaDispMatutinaModif = tablaDispMatutinaModif;
	}

	public UIDataTable getTablaDispVespertinaModif() {
		return tablaDispVespertinaModif;
	}

	public void setTablaDispVespertinaModif(UIDataTable tablaDispVespertinaModif) {
		this.tablaDispVespertinaModif = tablaDispVespertinaModif;
	}	
	
	
	public void beforePhaseGenerarDisponibilidades (PhaseEvent event) {

		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			sessionMBean.setPantallaTitulo("Generar Disponibilidades para un Período");
		}
	}

	public void seleccionarCupoMat(ActionEvent e) {
		genDispSessionMBean.setDispSeleccionado(null);
		@SuppressWarnings("unchecked")
		DisponibilidadReserva dr = ((Row<DisponibilidadReserva>)this.getTablaDispMatutinaModif().getRowData()).getData();
  	
		if (dr != null) {
			genDispSessionMBean.setDispSeleccionado(dr); 
			dispAux.setCupo(dr.getCupo());
			dispAux.setHoraInicio(dr.getHoraInicio());
		}
	}

	public void seleccionarCupoVesp(ActionEvent e) {
		genDispSessionMBean.setDispSeleccionado(null);
		@SuppressWarnings("unchecked")
		DisponibilidadReserva dr = ((Row<DisponibilidadReserva>)this.getTablaDispVespertinaModif().getRowData()).getData();
  	
		if (dr != null) {
			genDispSessionMBean.setDispSeleccionado(dr); 
			dispAux.setCupo(dr.getCupo());
			dispAux.setHoraInicio(dr.getHoraInicio());
		}
	}
	
	public void generarDisponibilidades(ActionEvent event){
		VentanaDeTiempo v = new VentanaDeTiempo();
		Boolean huboError = false;
		
		if (genDispSessionMBean.getFechaModelo() == null ) {
			addErrorMessage("La fecha Modelo no puede ser nula", MSG_ID);
			huboError = true;
		}
		
		if (genDispSessionMBean.getFechaInicial() == null ) {
			addErrorMessage("La fecha Inicial no puede ser nula", MSG_ID);
			huboError = true;
		}

		if (genDispSessionMBean.getFechaFinal() == null ) {
			addErrorMessage("La fecha Final no puede ser nula", MSG_ID);
			huboError = true;
		}

		if (!huboError) {
			try{
			v.setFechaInicial(Utiles.time2InicioDelDia(genDispSessionMBean.getFechaInicial()) );
			v.setFechaFinal(Utiles.time2FinDelDia(genDispSessionMBean.getFechaFinal()));
			disponibilidadesEJB.generarDisponibilidades(sessionMBean.getRecursoMarcado(),genDispSessionMBean.getFechaModelo(), v);
			addInfoMessage("Disponibilidades generadas correctamente.", MSG_ID);
			}
			catch (OptimisticLockException lockE){
				addErrorMessage(lockE, "Al generar disponibilidades, el recurso no se encontraba disponible. Intente más tarde.");
			}
			catch (PersistenceException persE){
				addErrorMessage(persE, "Al generar disponibilidades, el recurso no se encontraba disponible. Intente más tarde.");				
			}
			catch (EJBException eE){
				if (eE.getCause() instanceof OptimisticLockException){
					addErrorMessage("Al generar disponibilidades, el recurso no se encontraba disponible. Intente más tarde.", MSG_ID);					
				}
				else{
					addErrorMessage(eE, MSG_ID);
					}
			}
			catch (Exception e) {
				addErrorMessage(e, MSG_ID);
			}

		}
		
	}

	public GenDispSessionMBean getGenDispSessionMBean() {
		return genDispSessionMBean;
	}

	public void setGenDispSessionMBean(GenDispSessionMBean genDispSessionMBean) {
		this.genDispSessionMBean = genDispSessionMBean;
	}

	/*
	public void cancelarModifDisp(ActionEvent event) {

		//Esto es el código del actionListener obtenerCuposModif
		VentanaDeTiempo v = new VentanaDeTiempo();
		if (genDispSessionMBean.getFechaModifCupo() != null ) {
			//Se setea hora 00:00:00
			v.setFechaInicial(Utiles.time2InicioDelDia(genDispSessionMBean.getFechaModelo()) );
			v.setFechaFinal(Utiles.time2FinDelDia(genDispSessionMBean.getFechaModifCupo()));
			genDispSessionMBean.setDisponibilidadesDelDiaMatutina(null);
			genDispSessionMBean.setDisponibilidadesDelDiaVespertina(null) ;
			obtenerCupos(v);
			configurarDisponibilidadesDelDiaModif();
		}
		else{
			addErrorMessage("La fecha no puede ser nula", MSG_ID);
		}

		genDispSessionMBean.setDispSeleccionado(null);
	}
	*/
/*
	public void guardarModifDisp(ActionEvent event) {
		if (genDispSessionMBean.getDispSeleccionado() != null) {			
 			try {
 
 				Disponibilidad d = new Disponibilidad();
 				d.setId(genDispSessionMBean.getDispSeleccionado().getId());
// 				d.setCupo(dispSessionMBean.getDispSeleccionado().getCupo());
 				d.setCupo(dispAux.getCupo());
 				dispAux.setHoraInicio(null);
 				dispAux.setCupo(null);
 				disponibilidadesEJB.modificarCupoDeDisponibilidad(d);
 				
 				VentanaDeTiempo v = new VentanaDeTiempo();
				//Se setea hora 00:00:00
				v.setFechaInicial(Utiles.time2InicioDelDia(genDispSessionMBean.getFechaModifCupo()) );
				v.setFechaFinal(Utiles.time2FinDelDia(genDispSessionMBean.getFechaModifCupo()));
				genDispSessionMBean.setDisponibilidadesDelDiaMatutina(null);
				genDispSessionMBean.setDisponibilidadesDelDiaVespertina(null) ;
				obtenerCupos(v);
				configurarDisponibilidadesDelDiaModif();

 				addInfoMessage("Cupo modificado correctamente.", MSG_ID); 				
 			} catch (Exception e) {
 				addErrorMessage(e, MSG_ID);
 			}
		}
		else {
			addErrorMessage("Debe seleccionar un dato del recurso", MSG_ID);
		}
	}
	*/

}
