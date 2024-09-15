package br.com.papaprecoapi.rest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.papaprecoapi.dto.AlertaUsuarioDTO;
import br.com.papaprecoapi.model.AlertaUsuario;
import br.com.papaprecoapi.repository.AlertaUsuarioRepository;

@CrossOrigin
@RestController
@RequestMapping("alertas")
public class AlertaUsuarioController {
    @Autowired
    private AlertaUsuarioRepository repo;

    @Autowired
    private ModelMapper mapper;

    @GetMapping(value = "/usuario/{usuarioId}", produces = "application/json;charset=UTF-8")
    public List<AlertaUsuarioDTO> findAllByUsuario(@PathVariable("usuarioId") Integer usuarioId) {

        List<AlertaUsuario> lista = repo.findByUsuario_id(usuarioId);     

        return lista.stream().map(e -> mapper.map(e,AlertaUsuarioDTO.class)).collect(Collectors.toList());
    }

    @PostMapping(produces = "application/json;charset=UTF-8")
    public AlertaUsuarioDTO inserir(@RequestBody AlertaUsuarioDTO alerta) {
        AlertaUsuario c = mapper.map(alerta, AlertaUsuario.class);
        c = repo.save(c);      
        return mapper.map(c, AlertaUsuarioDTO.class);
    }

    @PutMapping(value = "/{id}", produces = "application/json;charset=UTF-8")
    public AlertaUsuarioDTO update(@PathVariable Integer id, @RequestBody AlertaUsuarioDTO alerta){
        Optional<AlertaUsuario> optionalAlerta = repo.findById(id);

        if(optionalAlerta.isPresent()) {
            AlertaUsuario existingAlerta = optionalAlerta.get();

            existingAlerta.setProduto(alerta.getProduto());
            existingAlerta.setPreco(alerta.getPreco());

            repo.save(existingAlerta);

            return mapper.map(existingAlerta , AlertaUsuarioDTO.class);
        } else {
            return null;
        }

    }

    @DeleteMapping(value = "/{id}", produces = "application/json;charset=UTF-8")
    public AlertaUsuarioDTO deletaAlertaUsuario(@PathVariable Integer id){
        Optional<AlertaUsuario> optionalAlertaUsuario = repo.findById(id);

        if(optionalAlertaUsuario.isPresent()){
            repo.deleteById(id);
        }

        return mapper.map(optionalAlertaUsuario, AlertaUsuarioDTO.class);
    }
}
