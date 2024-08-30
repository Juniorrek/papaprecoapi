package br.com.papaprecoapi.rest;

import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.papaprecoapi.dto.VotoUsuarioProdutoDTO;
import br.com.papaprecoapi.model.VotoUsuarioProduto;
import br.com.papaprecoapi.repository.VotoUsuarioProdutoRepository;

@CrossOrigin
@RestController
public class VotoUsuarioProdutoREST {

    @Autowired
    private VotoUsuarioProdutoRepository repo;

    @Autowired
    private ModelMapper mapper;
    
    @GetMapping(value = "/voto/{id}", produces = "application/json;charset=UTF-8")
    public VotoUsuarioProdutoDTO getById(@PathVariable Integer id) {

        VotoUsuarioProduto v = repo.findById(id).get();     

        return mapper.map(v, VotoUsuarioProdutoDTO.class);
    }
    
    @GetMapping(value = "/voto/{idUsuario}/{idProduto}", produces = "application/json;charset=UTF-8")
    public ResponseEntity<Object> getByIdUsuarioAndIdProduto(@PathVariable int idUsuario, @PathVariable int idProduto) {
        VotoUsuarioProduto v = repo.findByUsuario_IdAndProduto_Id(idUsuario, idProduto);     

        if (v == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        VotoUsuarioProdutoDTO dto = mapper.map(v, VotoUsuarioProdutoDTO.class);
        return ResponseEntity.ok(dto);
    }
    
    @PostMapping(value = "/votar", produces = "application/json;charset=UTF-8")
    public VotoUsuarioProdutoDTO inserir(@RequestBody VotoUsuarioProdutoDTO produto) {
        // salva a Entidade convertida do DTO
        VotoUsuarioProduto v = mapper.map(produto, VotoUsuarioProduto.class);
        v = repo.save(v);      
        // busca o usuário inserido
        Optional<VotoUsuarioProduto> produt = repo.findById(v.getId());
        // retorna o DTO equivalente à entidade
        return mapper.map(produt, VotoUsuarioProdutoDTO.class);
    }

    @PutMapping(value = "/voto/{idUsuario}/{idProduto}", produces = "application/json;charset=UTF-8")
    public VotoUsuarioProdutoDTO atualizar(@PathVariable int idUsuario, @PathVariable int idProduto, @RequestParam boolean novoVoto){
        VotoUsuarioProduto voto = repo.findByUsuario_IdAndProduto_Id(idUsuario, idProduto);

        if (voto == null) return null;

        voto.setVoto(novoVoto);
        voto = repo.save(voto);
        return mapper.map(voto , VotoUsuarioProdutoDTO.class);
    }
}
