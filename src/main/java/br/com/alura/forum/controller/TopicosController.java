package br.com.alura.forum.controller;

import br.com.alura.forum.controller.request.AtualizacaoTopicoRequest;
import br.com.alura.forum.controller.request.TopicoRequest;
import br.com.alura.forum.controller.response.DetalhesTopicoResponse;
import br.com.alura.forum.controller.response.TopicoResponse;
import br.com.alura.forum.modelo.Topico;
import br.com.alura.forum.repository.CursoRepository;
import br.com.alura.forum.repository.TopicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/topicos")
public class TopicosController {

    @Autowired
    private TopicoRepository topicoRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @GetMapping
    @Cacheable(value = "listaDeTopicos")
    public Page<TopicoResponse> lista(@RequestParam(required = false) String nomeCurso,
                                      @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable paginacao
    ) {
        Page<Topico> topicos;

        if(nomeCurso == null) {
            topicos = topicoRepository.findAll(paginacao);
        } else {
            topicos = topicoRepository.findByCursoNome(nomeCurso, paginacao);
        }

        return TopicoResponse.converter(topicos);
    }

    @PostMapping
    @CacheEvict(value = "listaDeTopicos", allEntries = true)
    public ResponseEntity<TopicoResponse> cadastrar(@RequestBody @Valid TopicoRequest topicoRequest, UriComponentsBuilder uriBuilder) {
        Topico topico = topicoRequest.converter(cursoRepository);
        topicoRepository.save(topico);

        URI uri = uriBuilder.path("/topicos/{id}").buildAndExpand(topico.getId()).toUri();
        return ResponseEntity.created(uri).body(new TopicoResponse(topico));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetalhesTopicoResponse> detalhar(@PathVariable Long id) {
        Optional<Topico> topico = topicoRepository.findById(id);

        if(!topico.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(new DetalhesTopicoResponse(topico.get()));
    }

    @PutMapping("/{id}")
    @CacheEvict(value = "listaDeTopicos", allEntries = true)
    public ResponseEntity<TopicoResponse> atualizar(@PathVariable Long id, @RequestBody @Valid AtualizacaoTopicoRequest topicoRequest) {
        Topico topico = topicoRequest.atualizar(id, topicoRepository);

        if(topico != null) {
            topicoRepository.save(topico);
            return ResponseEntity.ok(new TopicoResponse(topico));
        }

        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = "listaDeTopicos", allEntries = true)
    public ResponseEntity<?> remover(@PathVariable Long id) {
        Optional<Topico> topico = topicoRepository.findById(id);

        if(!topico.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        topicoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
