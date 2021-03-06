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

package uy.gub.imm.sae.business.ejb.facade;

import java.util.Date;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import uy.gub.imm.sae.entity.Agenda;
import uy.gub.imm.sae.entity.TextoAgenda;
import uy.gub.imm.sae.exception.ApplicationException;
import uy.gub.imm.sae.exception.BusinessException;
import uy.gub.imm.sae.exception.UserException;

@Stateless
@RolesAllowed("RA_AE_ADMINISTRADOR")
public class AgendasBean implements AgendasLocal,  AgendasRemote{

	@PersistenceContext(unitName = "SAE-EJB")
	private EntityManager entityManager;

	/**
	 * Crea la agenda <b>a</b> en el sistema.
	 * Controla la unicidad del nombre de la agenda entre todas las agendas vivas (fechaBaja == null).
	 * Retorna la agenda con su identificador interno.
	 * Roles permitidos: Administrador
	 * @throws UserException 
	 * @throws ApplicationException 
	 * @throws BusinessException 
	 */
	public Agenda crearAgenda(Agenda a) throws UserException, ApplicationException, BusinessException {
		// TODO Auto-generated method stub
		//Se controla que no exista otra agenda con el mismo nombre
		if (existeAgendaPorNombre(a) ) {
				throw new UserException("AE10001","Ya existe la agenda de nombre "+ a.getNombre());
		}
		//Se controla que la agenda no tenga fecha de baja
		if (a.getFechaBaja() != null){
			throw new BusinessException("AE20005","No se puede dar de alta una agenda con fecha de baja: "+ a.getNombre());			
		}
		//Se controla que la descripcion no sea nula
		if (a.getDescripcion() == null || a.getDescripcion().equals("")){
			throw new UserException("AE10001","No se puede dar de alta una agenda sin descripción");			
		}
		//Se controla que no exista otra agenda con la misma descripcion
		if (existeAgendaPorDescripcion(a) ) {
				throw new UserException("AE10006","Ya existe una agenda con esa descripción: "+ a.getDescripcion());
		}
		
		if (a.getTextoAgenda() == null){
			TextoAgenda tnuevo = new TextoAgenda();
			a.setTextoAgenda(tnuevo);
			tnuevo.setAgenda(a);
		}

		entityManager.persist(a);

		return a;
	}

	/**
	 * Se realiza la baja logica de la agenda (se setea fechaBaja con la fecha actual del sistema).
	 * Controla que no existan recursos vivos para esta esta agenda, si es asi no se la da de baja.	 * 
	 * Controla que no existan reservas vivas para esta esta agenda, si es asi no se la da de baja.
	 * Roles permitidos: Administrador
	 * @throws UserException 
	 * @throws ApplicationException 
	 */
	public void eliminarAgenda(Agenda a) throws UserException, ApplicationException {
		// TODO Auto-generated method stub
		Agenda agenda = (Agenda) entityManager.find(Agenda.class, a.getId());
		
		if (agenda == null) {
			throw new UserException("AE10002","No existe la agenda que se quiere eliminar: " + a.getId().toString());
		}
		
		//Se controla que no existan recursos vivos para la agenda.
		 if  (hayRecursosVivos(agenda)) {
			throw new UserException("AE10003","Se encontró algun recurso vivo para la agenda: " + a.getId().toString());
		}

		//Se controla que no existan reservas vivas para la agenda.
		 if (hayReservasVivas(agenda)){
			throw new UserException("AE10004","Existen reservas vivas para esa agenda");
		}
		agenda.setFechaBaja(new Date());
	
	}

	/**
	 * Se realiza la modificacion de la agenda <b>a</b>.
	 * Controla la unicidad del nombre de la agenda entre todas las agendas vivas (fechaBaja == null).
	 * Roles permitidos: Administrador
	 * @throws UserException 
	 * @throws ApplicationException 
	 */
	public void modificarAgenda(Agenda a) throws UserException, ApplicationException {
		// TODO Auto-generated method stub
		Agenda agendaActual = (Agenda) entityManager.find(Agenda.class, a.getId());
		
		if (agendaActual == null) {
			throw new UserException("AE10002","No existe la agenda que se quiere modificar: " + a.getId().toString());
		}
			
		//Se controla que no exista otra agenda viva con el mismo nombre
		if (existeAgendaPorNombre(a) ) {
			throw new UserException("AE10001","Ya existe una agenda con ese nombre: "+ a.getNombre());
		}
		
		if (agendaActual.getFechaBaja()!= null) {
			throw new UserException("AE10070","La agenda esta dada de baja no se puede modificar");
		}
		
		
		
		//Se controla que el texto del paso 1 no supere el largo esperado
		//Se verifica previamente que el texto exista

		if ((a.getTextoAgenda().getTextoPaso1() != null) && (a.getTextoAgenda().getTextoPaso1().length() > 1000)){
			throw new UserException("AE10007","El texto del paso uno y su formato superan el largo esperado");			
		}

		
		//Se controla que el texto del paso 2 no supere el largo esperado
		//Se verifica previamente que el texto exista
		
		if ((a.getTextoAgenda().getTextoPaso2() != null) && (a.getTextoAgenda().getTextoPaso2().length() > 1000)){
				throw new UserException("AE10007","El texto del paso dos y su formato superan el largo esperado");			
		}
		
		
		//Se controla que el texto del paso 3 no supere el largo esperado
		//Se verifica previamente que el texto exista
		
		if ((a.getTextoAgenda().getTextoPaso3() != null) && (a.getTextoAgenda().getTextoPaso3().length() > 1000)){
				throw new UserException("AE10007","El texto del paso tres y su formato superan el largo esperado");			
		}
		
		
		//Se controla que la descripción no sea nula
		if (a.getDescripcion() == null || a.getDescripcion().equals("")){
			throw new UserException("AE10001","No se puede dar de alta una agenda sin descripción");			
		}
		//Se controla que no exista otra agenda con la misma descripción
		if (existeAgendaPorDescripcion(a) ) {
				throw new UserException("AE10006","Ya existe una agenda con esa descripcion: "+ a.getDescripcion());
		}

    	agendaActual.setNombre(a.getNombre());
    	agendaActual.setDescripcion(a.getDescripcion());
    	
		TextoAgenda texto = agendaActual.getTextoAgenda();
		texto.setTextoPaso1(a.getTextoAgenda().getTextoPaso1());
		texto.setTextoPaso2(a.getTextoAgenda().getTextoPaso2());
		texto.setTextoPaso3(a.getTextoAgenda().getTextoPaso3());
		texto.setTextoSelecRecurso(a.getTextoAgenda().getTextoSelecRecurso());
		texto.setTextoTicketConf(a.getTextoAgenda().getTextoTicketConf());
		
          
	}
	
	private Boolean hayRecursosVivos(Agenda a) throws ApplicationException{
		try{
		Long cant = (Long) entityManager
					.createQuery("SELECT count(r) FROM Recurso r WHERE r.agenda = :agenda AND r.fechaBaja IS NULL")
					.setParameter("agenda", a)
					.getSingleResult();
		
		return (cant > 0);
		}catch (Exception e){
			throw new ApplicationException(e);
		}
	}

	private Boolean hayReservasVivas(Agenda a) throws ApplicationException{
		try {Long cant = (Long) entityManager
					.createQuery("SELECT count(r) FROM Disponibilidad d JOIN d.reservas r " +
							"WHERE d.recurso.agenda = :agenda " +
							"  AND d.fecha >= :fecha" +
							"  AND d.horaFin >= :hora" +
							"  AND r.estado IN ('R','P')")
					.setParameter("agenda", a)
					.setParameter("fecha", new Date())
					.setParameter("hora", new Date())
					.getSingleResult();

		return (cant > 0);
		}catch (Exception e){
			throw new ApplicationException(e);
		}
	}
	
	private Boolean existeAgendaPorNombre(Agenda a) throws ApplicationException{
		try{
		Long cant = (Long) entityManager
								.createQuery("SELECT count(a) from Agenda a " +
										"WHERE upper(a.nombre) = upper(:nombre) " +
										"and (a.id <> :id or :id is null) " +
										"AND a.fechaBaja IS NULL")
								.setParameter("nombre", a.getNombre())
								.setParameter("id", a.getId())
								.getSingleResult();
		
		return (cant > 0);
		} catch (Exception e){
			throw new ApplicationException(e);
		}
	}

	private Boolean existeAgendaPorDescripcion(Agenda a) throws ApplicationException{
		try{
		Long cant = (Long) entityManager
								.createQuery("SELECT count(a) from Agenda a " +
										"WHERE upper(a.descripcion) = upper(:descripcion) " +
										"and (a.id <> :id or :id is null) " +
										"AND a.fechaBaja IS NULL")
								.setParameter("descripcion", a.getDescripcion())
								.setParameter("id", a.getId())
								.getSingleResult();
		
		return (cant > 0);
		} catch (Exception e){
			throw new ApplicationException(e);
		}
	}

}
