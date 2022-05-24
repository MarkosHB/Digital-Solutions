package es.uma.proyecto.ejb;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import es.uma.proyecto.ejb.Excepciones.ClienteExistenteException;
import es.uma.proyecto.ejb.Excepciones.ClienteNoExisteException;
import es.uma.proyecto.ejb.Excepciones.ClienteNoSuporteadoException;
import es.uma.proyecto.ejb.Excepciones.TieneCuentaAsociadoException;
import es.uma.proyecto.ejb.Excepciones.UsuarioNoEncontradoException;
import es.uma.proyecto.jpa.Cliente;
import es.uma.proyecto.jpa.Cuenta_Fintech;
import es.uma.proyecto.jpa.Empresa;
import es.uma.proyecto.jpa.Individual;
import es.uma.proyecto.jpa.Usuario;

@Stateless
public class ClientesEJB implements GestionClientes {

	@PersistenceContext(name = "proyecto-ejb")
	private EntityManager em;

	public void darDeAltaIndividual(Usuario u, Individual i) throws ClienteExistenteException {
		if (em.find(Empresa.class, i.getId()) != null || em.find(Individual.class, i.getId()) != null) {
			throw new ClienteExistenteException();
		}
		Usuario usu = em.find(Usuario.class, u.getUsuario());
		i.setTipo_cliente("FISICA");
		i.setUs(usu);
		em.persist(i);
	}

	public void darDeAltaEmpresa(Usuario u, Empresa e) throws ClienteExistenteException {
		if (em.find(Empresa.class, e.getId()) != null || em.find(Individual.class, e.getId()) != null) {
			throw new ClienteExistenteException();
		}

		Usuario usu = em.find(Usuario.class, u.getUsuario());
		e.setTipo_cliente("JURIDICA");
		e.setUs(usu);
		em.persist(e);
	}

	public void modificarCliente(Cliente cl) throws ClienteNoExisteException, ClienteNoSuporteadoException {

		if (cl instanceof Empresa) {
			Empresa ent = (Empresa) cl;

			if (em.find(Empresa.class, ent.getId()) == null) {
				throw new ClienteNoExisteException();
			}
			em.merge(ent);

		} else if (cl instanceof Individual) {
			Individual ind = (Individual) cl;
			if (em.find(Individual.class, ind.getId()) == null) {
				throw new ClienteNoExisteException();
			}
			em.merge(ind);

		} else {
			throw new ClienteNoSuporteadoException();
		}
	}
	
	public void darDeBajaIndividual(Individual i) throws ClienteNoExisteException, TieneCuentaAsociadoException {
		Individual ind=em.find(Individual.class, i.getId());
		if(ind==null) {
			throw new ClienteNoExisteException();
		}

		for (Cuenta_Fintech c : ind.getCf()) {
			if (!c.getEstado().equalsIgnoreCase("Baja")) {
				throw new TieneCuentaAsociadoException();
			}
		}

		ind.setEstado("Baja");
	}
	
	public void darDeBajaEmpresa(Empresa e) throws ClienteNoExisteException, TieneCuentaAsociadoException {
		Empresa ent=em.find(Empresa.class, e.getId());
		if(ent==null) {
			throw new ClienteNoExisteException();
		}

		for (Cuenta_Fintech c : ent.getCf()) {
			if (!c.getEstado().equalsIgnoreCase("Baja")) {
				throw new TieneCuentaAsociadoException();
			}
		}

		ent.setEstado("Baja");
	}
	
	public void bloquearCliente(Cliente cl) throws ClienteNoExisteException, ClienteNoSuporteadoException {
		if(cl instanceof Empresa) {
			Empresa emp=em.find(Empresa.class, cl.getId());
			if(emp==null) {
				throw new ClienteNoExisteException();
			}
			
			emp.setEstado("BLOQUEADO");
		}else if(cl instanceof Individual) {
			Individual ind=em.find(Individual.class, cl.getId());
			if(ind==null) {
				throw new ClienteNoExisteException();
			}
			
			ind.setEstado("BLOQUEADO");
		}else {
			throw new ClienteNoSuporteadoException();
		}
	}
	
	public void desbloquearCliente(Cliente cl) throws ClienteNoExisteException, ClienteNoSuporteadoException {
		if(cl instanceof Empresa) {
			Empresa emp=em.find(Empresa.class, cl.getId());
			if(emp==null) {
				throw new ClienteNoExisteException();
			}
			
			emp.setEstado("ALTA");
		}else if(cl instanceof Individual) {
			Individual ind=em.find(Individual.class, cl.getId());
			if(ind==null) {
				throw new ClienteNoExisteException();
			}
			
			ind.setEstado("ALTA");
		}else {
			throw new ClienteNoSuporteadoException();
		}
	}

	public List<Empresa> sacarEmpresas() {
		return em.createQuery("SELECT ent FROM Empresa ent", Empresa.class).getResultList();
	}

	public List<Individual> sacarIndividual() {
		return em.createQuery("SELECT i FROM Individual i", Individual.class).getResultList();
	}
	
	public List<Cliente> sacarClientes() {
		return em.createQuery("SELECT i FROM Cliente i", Cliente.class).getResultList();
	}
	
	public Individual gtIndividual(Long id) {
		return em.find(Individual.class,id);
		}
	
	public Empresa gtEmpresa(Long id) {
		return em.find(Empresa.class,id);
		}

}