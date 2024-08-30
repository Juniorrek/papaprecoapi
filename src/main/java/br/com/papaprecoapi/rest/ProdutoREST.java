package br.com.papaprecoapi.rest;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.papaprecoapi.dto.ProdutoDTO;
import br.com.papaprecoapi.model.Localizacao;
import br.com.papaprecoapi.model.Produto;
import br.com.papaprecoapi.repository.LocalizacaoRepository;
import br.com.papaprecoapi.repository.ProdutoRepository;
import br.com.papaprecoapi.services.NominatimService;

@CrossOrigin
@RestController
public class ProdutoREST {

    @Autowired
    private ProdutoRepository repo;

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private NominatimService nominatimService;

    @Autowired
    private LocalizacaoRepository localizacaoRepository;
    
    @GetMapping(value = "/produtos")// , produces = "application/json;charset=UTF-8")
    public List<ProdutoDTO> buscarTodos(){

        List<Produto> lista = repo.findAll();     

        return lista.stream().map(e -> mapper.map(e, ProdutoDTO.class)).collect(Collectors.toList());
    }
    
    @GetMapping(value = "/produtos/{id}" , produces = "application/json;charset=UTF-8")
    public ProdutoDTO buscarPorId(@PathVariable("id") Integer id){

        Produto produto = repo.findById(id).get();     

        return mapper.map(produto, ProdutoDTO.class);
    }
    
    @GetMapping(value = "/produtos/nome/{nome}" , produces = "application/json;charset=UTF-8")
    public List<ProdutoDTO> buscarPorNome(@PathVariable("nome") String nome){

        List<Produto> lista = repo.findByNomeContainingIgnoringCase(nome);     

        return lista.stream().map(e -> mapper.map(e, ProdutoDTO.class)).collect(Collectors.toList());
    }
    
    @GetMapping(value = "/produtos/filtrar" , produces = "application/json;charset=UTF-8")
    public List<ProdutoDTO> filtrar(@RequestParam("nome") String nome ,
                                    @RequestParam("latitude") Double latitude,
                                    @RequestParam("longitude") Double longitude ,
                                    @RequestParam("distancia") Double distancia,
                                    @RequestParam("precoMin") Double precoMin,
                                    @RequestParam("precoMax") Double precoMax){

        List<Produto> lista = repo.findByNomeContainingIgnoreCaseAndPrecoBetween(nome, precoMin, precoMax); 
        
        lista = lista.stream()
                 .filter(p -> calculateDistance(p.getLocalizacao().getLatitude(), p.getLocalizacao().getLongitude(), latitude, longitude) <= distancia)
                 .collect(Collectors.toList());

        return lista.stream().map(e -> mapper.map(e, ProdutoDTO.class)).collect(Collectors.toList());
    }

    double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double EARTH_RADIUS = 6371;

        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double lon1Rad = Math.toRadians(lon1);
        double lon2Rad = Math.toRadians(lon2);
    
        double x = (lon2Rad - lon1Rad) * Math.cos((lat1Rad + lat2Rad) / 2);
        double y = (lat2Rad - lat1Rad);
        double distance = Math.sqrt(x * x + y * y) * EARTH_RADIUS;
    
        return distance;
    }
    
    @GetMapping(value = "/produtos/ranking" , produces = "application/json;charset=UTF-8")
    public List<ProdutoDTO> buscarProdutosPorPalavraEPrecoRanking(@RequestParam("palavra") String palavra ,
                                    @RequestParam("latitude") Double latitude,
                                    @RequestParam("longitude") Double longitude,
                                    @RequestParam("distancia") Double distancia,
                                    @RequestParam("precoMin") Double precoMin,
                                    @RequestParam("precoMax") Double precoMax){

        //FILTRA PRECO E MATCH PALAVRA-NOME LEVENSHTEIN / AGRUPA PRODUTOS COM MESMO NOME LAT E LON E MOSTRA O MAIOR NO RANKING
        List<Produto> lista = repo.buscarProdutosPorPalavraEPrecoRanking(palavra, precoMin, precoMax); 
        
        //FILTRA DISTANCIA
        lista = lista.stream()
                 .filter(p -> calculateDistance(p.getLocalizacao().getLatitude(), p.getLocalizacao().getLongitude(), latitude, longitude) <= distancia)
                 .collect(Collectors.toList());

        // Mapeia para ProdutoDTO e adiciona a distância e a data relativa
        return lista.stream()
            .map(produto -> {
                double distanciaCalculada = calculateDistance(produto.getLocalizacao().getLatitude(),
                                                              produto.getLocalizacao().getLongitude(),
                                                              latitude, longitude);

                if (distanciaCalculada <= distancia) {
                    ProdutoDTO dto = mapper.map(produto, ProdutoDTO.class);
                    dto.setDistanciaRelativa(distanciaCalculada);

                    // Calcula e define a data relativa
                    String dataRelativa = calcularDataRelativa(produto.getDataObservacao());
                    dto.setDataRelativa(dataRelativa);

                    return dto;
                } else {
                    return null;  
                }
            })
            .filter(Objects::nonNull) 
            .collect(Collectors.toList());
    }

    private String calcularDataRelativa(LocalDateTime dataObservacao) {
        Duration duration = Duration.between(dataObservacao, LocalDateTime.now());
        long dias = duration.toDays();

        if (dias == 0) {
            return "hoje";
        } else if (dias == 1) {
            return "ontem";
        } else if (dias < 7) {
            return "há " + dias + " dias";
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return dataObservacao.format(formatter);
        }
    }
    
    @GetMapping(value = "/produtos/historico" , produces = "application/json;charset=UTF-8")
    public List<ProdutoDTO> buscarHistoricoProdutoRanking(@RequestParam("nome") String nome ,
                                    @RequestParam("latitude") Double latitude,
                                    @RequestParam("longitude") Double longitude){

        List<Produto> lista = repo.buscarHistoricoProdutoRanking(nome, latitude, longitude);

        return lista.stream().map(e -> mapper.map(e, ProdutoDTO.class)).collect(Collectors.toList());
    }
    
    @GetMapping(value = "/produtos/atual" , produces = "application/json;charset=UTF-8")
    public ProdutoDTO buscarProdutoAtualRanking(@RequestParam("nome") String nome ,
                                    @RequestParam("latitude") Double latitude,
                                    @RequestParam("longitude") Double longitude){

        List<Produto> lista = repo.buscarHistoricoProdutoRanking(nome, latitude, longitude);

        return mapper.map(lista.get(0), ProdutoDTO.class);
    }

    @PostMapping(value = "/produtos", produces = "application/json;charset=UTF-8")
    public ProdutoDTO inserir(@RequestBody ProdutoDTO produto) {
        // salva a Entidade convertida do DTO
        Produto p = mapper.map(produto, Produto.class);

        Localizacao l = localizacaoRepository.findByLatitudeAndLongitude(p.getLocalizacao().getLatitude(), p.getLocalizacao().getLongitude());
        if (l == null) {
            l = localizacaoRepository.save(p.getLocalizacao());
        }
        p.setLocalizacao(l);

        p = repo.save(p);     

        // busca o usuário inserido
        //Optional<Produto> produt = repo.findById(p.getId());
        // retorna o DTO equivalente à entidade
        return mapper.map(p, ProdutoDTO.class);
    }

    @PostMapping(value = "/produtos/lista", produces = "application/json;charset=UTF-8")
    public ResponseEntity<?> inserirLista(@RequestBody List<ProdutoDTO> produtos) {
        // salva a Entidade convertida do DTO
        Type produtoListType = new TypeToken<List<Produto>>() {}.getType();
        List<Produto> prods = mapper.map(produtos, produtoListType);

        if (!prods.isEmpty()) {
            Localizacao l = localizacaoRepository.findByLatitudeAndLongitude(prods.get(0).getLocalizacao().getLatitude(), prods.get(0).getLocalizacao().getLongitude());
            if (l == null) {
                l = localizacaoRepository.save(prods.get(0).getLocalizacao());
            }
            
            for(Produto p : prods) {
                p.setLocalizacao(l);
                p = repo.save(p);  
            }

        }

        return ResponseEntity.ok().build();
    }
}
