package org.jhipster.blog.web.rest;

import org.jhipster.blog.domain.Blog;
import org.jhipster.blog.repository.BlogRepository;
import org.jhipster.blog.security.SecurityUtils;
import org.jhipster.blog.web.rest.errors.BadRequestAlertException;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link org.jhipster.blog.domain.Blog}.
 */
@RestController
@RequestMapping("/api")
public class BlogResource {

    private final Logger log = LoggerFactory.getLogger(BlogResource.class);

    private static final String ENTITY_NAME = "blog";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final BlogRepository blogRepository;
    private final EntityManager entityManager;

    @Autowired
    public BlogResource(BlogRepository blogRepository, EntityManager entityManager) {
        this.blogRepository = blogRepository;
		this.entityManager = entityManager;
    }

    /**
     * {@code POST  /blogs} : Create a new blog.
     *
     * @param blog the blog to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new blog, or with status {@code 400 (Bad Request)} if the blog has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @Transactional
    @PostMapping("/blogs")
    public ResponseEntity<?> createBlog(@Valid @RequestBody Blog blog) throws URISyntaxException {
        log.debug("REST request to save Blog : {}", blog);
        if (blog.getId() != null) {
            throw new BadRequestAlertException("A new blog cannot already have an ID", ENTITY_NAME, "idexists");
        }
        if (!blog.getUser().getLogin().equals(SecurityUtils.getCurrentUserLogin().orElse(""))) {
            return new ResponseEntity<>("error.http.403", HttpStatus.FORBIDDEN);
        }
        Blog result = blogRepository.save(blog);
        return ResponseEntity.created(new URI("/api/blogs/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /blogs} : Updates an existing blog.
     *
     * @param blog the blog to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated blog,
     * or with status {@code 400 (Bad Request)} if the blog is not valid,
     * or with status {@code 500 (Internal Server Error)} if the blog couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @Transactional
    @PutMapping("/blogs")
    public ResponseEntity<?> updateBlog(@Valid @RequestBody Blog blog) throws URISyntaxException {
        log.debug("REST request to update Blog : {}", blog);
        if (blog.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        Blog oldBlog = (Blog) entityManager.createQuery("SELECT blog from Blog blog where blog.id = :id")
        .setParameter("id", blog.getId())
        .getSingleResult();
        
        if (!oldBlog.getName().equals(blog.getName()))
        {
        	String query = "UPDATE Blog SET name = '" + blog.getName() + "'"
        	        +" WHERE id = " + blog.getId();
        	        entityManager.createQuery(query)
        	        .executeUpdate(); 
        }
        if (!oldBlog.getHandle().equals(blog.getHandle()))
        {
        	String query = "UPDATE Blog SET handle = '" + blog.getHandle() + "'"
        	        +" WHERE id = " + blog.getId();
        	        entityManager.createQuery(query)
        	        .executeUpdate(); 
        }
        if (!oldBlog.getUser().equals(blog.getUser()))
        {
        	String query = "UPDATE Blog SET user = :user"
        	        +" WHERE id = " + blog.getId();
        	        entityManager.createQuery(query)
        	        .setParameter("user", blog.getUser())
        	        .executeUpdate(); 
        } 
        
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, blog.getId().toString()))
            .body(blog);
    }

    /**
     * {@code GET  /blogs} : get all the blogs.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of blogs in body.
     */
    @SuppressWarnings("unchecked")
	@GetMapping("/blogs")
    public List<Blog> getAllBlogs() {
        log.debug("REST request to get all Blogs");
        return entityManager.createQuery("SELECT blog from Blog blog where blog.user.login = :userName")
        		.setParameter("userName", SecurityUtils.getCurrentUserLogin().get()).getResultList();
    }

    /**
     * {@code GET  /blogs/:id} : get the "id" blog.
     *
     * @param id the id of the blog to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the blog, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/blogs/{id}")
    public ResponseEntity<?> getBlog(@PathVariable Long id) {
        log.debug("REST request to get Blog : {}", id);
        Blog blog = (Blog) entityManager.createQuery("SELECT blog from Blog blog where blog.id = :id")
		.setParameter("id", id)
		.getSingleResult();
        
        Optional<Blog> optionalBlog = Optional.of(blog);
        if (optionalBlog.isPresent() && optionalBlog.get().getUser() != null &&
            !optionalBlog.get().getUser().getLogin().equals(SecurityUtils.getCurrentUserLogin().orElse(""))) {
            return new ResponseEntity<>("error.http.403", HttpStatus.FORBIDDEN);
        }
        return ResponseUtil.wrapOrNotFound(optionalBlog);
    }

    /**
     * {@code DELETE  /blogs/:id} : delete the "id" blog.
     *
     * @param id the id of the blog to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @Transactional
    @DeleteMapping("/blogs/{id}")
    public ResponseEntity<?> deleteBlog(@PathVariable Long id) {
        log.debug("REST request to delete Blog : {}", id);
        Blog blog = (Blog) entityManager.createQuery("SELECT blog from Blog blog where blog.id = :id")
        .setParameter("id", id)
        .getSingleResult();
        
        Optional<Blog> optionalBlog = Optional.of(blog);
        if (optionalBlog.isPresent() && optionalBlog.get().getUser() != null &&
            !optionalBlog.get().getUser().getLogin().equals(SecurityUtils.getCurrentUserLogin().orElse(""))) {
            return new ResponseEntity<>("error.http.403", HttpStatus.FORBIDDEN);
        }
        entityManager.createQuery("DELETE from Blog WHERE id = :id")
        .setParameter("id", id)
        .executeUpdate();
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
