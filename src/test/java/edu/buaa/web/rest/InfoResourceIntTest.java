package edu.buaa.web.rest;

import edu.buaa.Edge2App;

import edu.buaa.domain.Info;
import edu.buaa.repository.InfoRepository;
import edu.buaa.service.InfoService;
import edu.buaa.web.rest.errors.ExceptionTranslator;
import edu.buaa.service.InfoQueryService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;
import org.springframework.validation.Validator;

import javax.persistence.EntityManager;
import java.util.List;


import static edu.buaa.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the InfoResource REST controller.
 *
 * @see InfoResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Edge2App.class)
public class InfoResourceIntTest {

    private static final String DEFAULT_FILE_NAME = "AAAAAAAAAA";
    private static final String UPDATED_FILE_NAME = "BBBBBBBBBB";

    private static final Long DEFAULT_FILE_SIZE = 1L;
    private static final Long UPDATED_FILE_SIZE = 2L;

    private static final String DEFAULT_FILE_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_FILE_TYPE = "BBBBBBBBBB";

    private static final byte[] DEFAULT_FILE_BODY = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_FILE_BODY = TestUtil.createByteArray(1, "1");
    private static final String DEFAULT_FILE_BODY_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_FILE_BODY_CONTENT_TYPE = "image/png";

    private static final String DEFAULT_NOTE = "AAAAAAAAAA";
    private static final String UPDATED_NOTE = "BBBBBBBBBB";

    @Autowired
    private InfoRepository infoRepository;

    @Autowired
    private InfoService infoService;

    @Autowired
    private InfoQueryService infoQueryService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private Validator validator;

    private MockMvc restInfoMockMvc;

    private Info info;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final InfoResource infoResource = new InfoResource(infoService, infoQueryService);
        this.restInfoMockMvc = MockMvcBuilders.standaloneSetup(infoResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Info createEntity(EntityManager em) {
        Info info = new Info()
            .file_name(DEFAULT_FILE_NAME)
            .file_size(DEFAULT_FILE_SIZE)
            .file_type(DEFAULT_FILE_TYPE)
            .file_body(DEFAULT_FILE_BODY)
            .file_bodyContentType(DEFAULT_FILE_BODY_CONTENT_TYPE)
            .note(DEFAULT_NOTE);
        return info;
    }

    @Before
    public void initTest() {
        info = createEntity(em);
    }

    @Test
    @Transactional
    public void createInfo() throws Exception {
        int databaseSizeBeforeCreate = infoRepository.findAll().size();

        // Create the Info
        restInfoMockMvc.perform(post("/api/infos")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(info)))
            .andExpect(status().isCreated());

        // Validate the Info in the database
        List<Info> infoList = infoRepository.findAll();
        assertThat(infoList).hasSize(databaseSizeBeforeCreate + 1);
        Info testInfo = infoList.get(infoList.size() - 1);
        assertThat(testInfo.getFile_name()).isEqualTo(DEFAULT_FILE_NAME);
        assertThat(testInfo.getFile_size()).isEqualTo(DEFAULT_FILE_SIZE);
        assertThat(testInfo.getFile_type()).isEqualTo(DEFAULT_FILE_TYPE);
        assertThat(testInfo.getFile_body()).isEqualTo(DEFAULT_FILE_BODY);
        assertThat(testInfo.getFile_bodyContentType()).isEqualTo(DEFAULT_FILE_BODY_CONTENT_TYPE);
        assertThat(testInfo.getNote()).isEqualTo(DEFAULT_NOTE);
    }

    @Test
    @Transactional
    public void createInfoWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = infoRepository.findAll().size();

        // Create the Info with an existing ID
        info.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restInfoMockMvc.perform(post("/api/infos")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(info)))
            .andExpect(status().isBadRequest());

        // Validate the Info in the database
        List<Info> infoList = infoRepository.findAll();
        assertThat(infoList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllInfos() throws Exception {
        // Initialize the database
        infoRepository.saveAndFlush(info);

        // Get all the infoList
        restInfoMockMvc.perform(get("/api/infos?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(info.getId().intValue())))
            .andExpect(jsonPath("$.[*].file_name").value(hasItem(DEFAULT_FILE_NAME.toString())))
            .andExpect(jsonPath("$.[*].file_size").value(hasItem(DEFAULT_FILE_SIZE.intValue())))
            .andExpect(jsonPath("$.[*].file_type").value(hasItem(DEFAULT_FILE_TYPE.toString())))
            .andExpect(jsonPath("$.[*].file_bodyContentType").value(hasItem(DEFAULT_FILE_BODY_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].file_body").value(hasItem(Base64Utils.encodeToString(DEFAULT_FILE_BODY))))
            .andExpect(jsonPath("$.[*].note").value(hasItem(DEFAULT_NOTE.toString())));
    }
    
    @Test
    @Transactional
    public void getInfo() throws Exception {
        // Initialize the database
        infoRepository.saveAndFlush(info);

        // Get the info
        restInfoMockMvc.perform(get("/api/infos/{id}", info.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(info.getId().intValue()))
            .andExpect(jsonPath("$.file_name").value(DEFAULT_FILE_NAME.toString()))
            .andExpect(jsonPath("$.file_size").value(DEFAULT_FILE_SIZE.intValue()))
            .andExpect(jsonPath("$.file_type").value(DEFAULT_FILE_TYPE.toString()))
            .andExpect(jsonPath("$.file_bodyContentType").value(DEFAULT_FILE_BODY_CONTENT_TYPE))
            .andExpect(jsonPath("$.file_body").value(Base64Utils.encodeToString(DEFAULT_FILE_BODY)))
            .andExpect(jsonPath("$.note").value(DEFAULT_NOTE.toString()));
    }

    @Test
    @Transactional
    public void getAllInfosByFile_nameIsEqualToSomething() throws Exception {
        // Initialize the database
        infoRepository.saveAndFlush(info);

        // Get all the infoList where file_name equals to DEFAULT_FILE_NAME
        defaultInfoShouldBeFound("file_name.equals=" + DEFAULT_FILE_NAME);

        // Get all the infoList where file_name equals to UPDATED_FILE_NAME
        defaultInfoShouldNotBeFound("file_name.equals=" + UPDATED_FILE_NAME);
    }

    @Test
    @Transactional
    public void getAllInfosByFile_nameIsInShouldWork() throws Exception {
        // Initialize the database
        infoRepository.saveAndFlush(info);

        // Get all the infoList where file_name in DEFAULT_FILE_NAME or UPDATED_FILE_NAME
        defaultInfoShouldBeFound("file_name.in=" + DEFAULT_FILE_NAME + "," + UPDATED_FILE_NAME);

        // Get all the infoList where file_name equals to UPDATED_FILE_NAME
        defaultInfoShouldNotBeFound("file_name.in=" + UPDATED_FILE_NAME);
    }

    @Test
    @Transactional
    public void getAllInfosByFile_nameIsNullOrNotNull() throws Exception {
        // Initialize the database
        infoRepository.saveAndFlush(info);

        // Get all the infoList where file_name is not null
        defaultInfoShouldBeFound("file_name.specified=true");

        // Get all the infoList where file_name is null
        defaultInfoShouldNotBeFound("file_name.specified=false");
    }

    @Test
    @Transactional
    public void getAllInfosByFile_sizeIsEqualToSomething() throws Exception {
        // Initialize the database
        infoRepository.saveAndFlush(info);

        // Get all the infoList where file_size equals to DEFAULT_FILE_SIZE
        defaultInfoShouldBeFound("file_size.equals=" + DEFAULT_FILE_SIZE);

        // Get all the infoList where file_size equals to UPDATED_FILE_SIZE
        defaultInfoShouldNotBeFound("file_size.equals=" + UPDATED_FILE_SIZE);
    }

    @Test
    @Transactional
    public void getAllInfosByFile_sizeIsInShouldWork() throws Exception {
        // Initialize the database
        infoRepository.saveAndFlush(info);

        // Get all the infoList where file_size in DEFAULT_FILE_SIZE or UPDATED_FILE_SIZE
        defaultInfoShouldBeFound("file_size.in=" + DEFAULT_FILE_SIZE + "," + UPDATED_FILE_SIZE);

        // Get all the infoList where file_size equals to UPDATED_FILE_SIZE
        defaultInfoShouldNotBeFound("file_size.in=" + UPDATED_FILE_SIZE);
    }

    @Test
    @Transactional
    public void getAllInfosByFile_sizeIsNullOrNotNull() throws Exception {
        // Initialize the database
        infoRepository.saveAndFlush(info);

        // Get all the infoList where file_size is not null
        defaultInfoShouldBeFound("file_size.specified=true");

        // Get all the infoList where file_size is null
        defaultInfoShouldNotBeFound("file_size.specified=false");
    }

    @Test
    @Transactional
    public void getAllInfosByFile_sizeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        infoRepository.saveAndFlush(info);

        // Get all the infoList where file_size greater than or equals to DEFAULT_FILE_SIZE
        defaultInfoShouldBeFound("file_size.greaterOrEqualThan=" + DEFAULT_FILE_SIZE);

        // Get all the infoList where file_size greater than or equals to UPDATED_FILE_SIZE
        defaultInfoShouldNotBeFound("file_size.greaterOrEqualThan=" + UPDATED_FILE_SIZE);
    }

    @Test
    @Transactional
    public void getAllInfosByFile_sizeIsLessThanSomething() throws Exception {
        // Initialize the database
        infoRepository.saveAndFlush(info);

        // Get all the infoList where file_size less than or equals to DEFAULT_FILE_SIZE
        defaultInfoShouldNotBeFound("file_size.lessThan=" + DEFAULT_FILE_SIZE);

        // Get all the infoList where file_size less than or equals to UPDATED_FILE_SIZE
        defaultInfoShouldBeFound("file_size.lessThan=" + UPDATED_FILE_SIZE);
    }


    @Test
    @Transactional
    public void getAllInfosByFile_typeIsEqualToSomething() throws Exception {
        // Initialize the database
        infoRepository.saveAndFlush(info);

        // Get all the infoList where file_type equals to DEFAULT_FILE_TYPE
        defaultInfoShouldBeFound("file_type.equals=" + DEFAULT_FILE_TYPE);

        // Get all the infoList where file_type equals to UPDATED_FILE_TYPE
        defaultInfoShouldNotBeFound("file_type.equals=" + UPDATED_FILE_TYPE);
    }

    @Test
    @Transactional
    public void getAllInfosByFile_typeIsInShouldWork() throws Exception {
        // Initialize the database
        infoRepository.saveAndFlush(info);

        // Get all the infoList where file_type in DEFAULT_FILE_TYPE or UPDATED_FILE_TYPE
        defaultInfoShouldBeFound("file_type.in=" + DEFAULT_FILE_TYPE + "," + UPDATED_FILE_TYPE);

        // Get all the infoList where file_type equals to UPDATED_FILE_TYPE
        defaultInfoShouldNotBeFound("file_type.in=" + UPDATED_FILE_TYPE);
    }

    @Test
    @Transactional
    public void getAllInfosByFile_typeIsNullOrNotNull() throws Exception {
        // Initialize the database
        infoRepository.saveAndFlush(info);

        // Get all the infoList where file_type is not null
        defaultInfoShouldBeFound("file_type.specified=true");

        // Get all the infoList where file_type is null
        defaultInfoShouldNotBeFound("file_type.specified=false");
    }

    @Test
    @Transactional
    public void getAllInfosByNoteIsEqualToSomething() throws Exception {
        // Initialize the database
        infoRepository.saveAndFlush(info);

        // Get all the infoList where note equals to DEFAULT_NOTE
        defaultInfoShouldBeFound("note.equals=" + DEFAULT_NOTE);

        // Get all the infoList where note equals to UPDATED_NOTE
        defaultInfoShouldNotBeFound("note.equals=" + UPDATED_NOTE);
    }

    @Test
    @Transactional
    public void getAllInfosByNoteIsInShouldWork() throws Exception {
        // Initialize the database
        infoRepository.saveAndFlush(info);

        // Get all the infoList where note in DEFAULT_NOTE or UPDATED_NOTE
        defaultInfoShouldBeFound("note.in=" + DEFAULT_NOTE + "," + UPDATED_NOTE);

        // Get all the infoList where note equals to UPDATED_NOTE
        defaultInfoShouldNotBeFound("note.in=" + UPDATED_NOTE);
    }

    @Test
    @Transactional
    public void getAllInfosByNoteIsNullOrNotNull() throws Exception {
        // Initialize the database
        infoRepository.saveAndFlush(info);

        // Get all the infoList where note is not null
        defaultInfoShouldBeFound("note.specified=true");

        // Get all the infoList where note is null
        defaultInfoShouldNotBeFound("note.specified=false");
    }
    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultInfoShouldBeFound(String filter) throws Exception {
        restInfoMockMvc.perform(get("/api/infos?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(info.getId().intValue())))
            .andExpect(jsonPath("$.[*].file_name").value(hasItem(DEFAULT_FILE_NAME)))
            .andExpect(jsonPath("$.[*].file_size").value(hasItem(DEFAULT_FILE_SIZE.intValue())))
            .andExpect(jsonPath("$.[*].file_type").value(hasItem(DEFAULT_FILE_TYPE)))
            .andExpect(jsonPath("$.[*].file_bodyContentType").value(hasItem(DEFAULT_FILE_BODY_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].file_body").value(hasItem(Base64Utils.encodeToString(DEFAULT_FILE_BODY))))
            .andExpect(jsonPath("$.[*].note").value(hasItem(DEFAULT_NOTE)));

        // Check, that the count call also returns 1
        restInfoMockMvc.perform(get("/api/infos/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultInfoShouldNotBeFound(String filter) throws Exception {
        restInfoMockMvc.perform(get("/api/infos?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restInfoMockMvc.perform(get("/api/infos/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("0"));
    }


    @Test
    @Transactional
    public void getNonExistingInfo() throws Exception {
        // Get the info
        restInfoMockMvc.perform(get("/api/infos/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateInfo() throws Exception {
        // Initialize the database
        infoService.save(info);

        int databaseSizeBeforeUpdate = infoRepository.findAll().size();

        // Update the info
        Info updatedInfo = infoRepository.findById(info.getId()).get();
        // Disconnect from session so that the updates on updatedInfo are not directly saved in db
        em.detach(updatedInfo);
        updatedInfo
            .file_name(UPDATED_FILE_NAME)
            .file_size(UPDATED_FILE_SIZE)
            .file_type(UPDATED_FILE_TYPE)
            .file_body(UPDATED_FILE_BODY)
            .file_bodyContentType(UPDATED_FILE_BODY_CONTENT_TYPE)
            .note(UPDATED_NOTE);

        restInfoMockMvc.perform(put("/api/infos")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedInfo)))
            .andExpect(status().isOk());

        // Validate the Info in the database
        List<Info> infoList = infoRepository.findAll();
        assertThat(infoList).hasSize(databaseSizeBeforeUpdate);
        Info testInfo = infoList.get(infoList.size() - 1);
        assertThat(testInfo.getFile_name()).isEqualTo(UPDATED_FILE_NAME);
        assertThat(testInfo.getFile_size()).isEqualTo(UPDATED_FILE_SIZE);
        assertThat(testInfo.getFile_type()).isEqualTo(UPDATED_FILE_TYPE);
        assertThat(testInfo.getFile_body()).isEqualTo(UPDATED_FILE_BODY);
        assertThat(testInfo.getFile_bodyContentType()).isEqualTo(UPDATED_FILE_BODY_CONTENT_TYPE);
        assertThat(testInfo.getNote()).isEqualTo(UPDATED_NOTE);
    }

    @Test
    @Transactional
    public void updateNonExistingInfo() throws Exception {
        int databaseSizeBeforeUpdate = infoRepository.findAll().size();

        // Create the Info

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restInfoMockMvc.perform(put("/api/infos")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(info)))
            .andExpect(status().isBadRequest());

        // Validate the Info in the database
        List<Info> infoList = infoRepository.findAll();
        assertThat(infoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteInfo() throws Exception {
        // Initialize the database
        infoService.save(info);

        int databaseSizeBeforeDelete = infoRepository.findAll().size();

        // Delete the info
        restInfoMockMvc.perform(delete("/api/infos/{id}", info.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Info> infoList = infoRepository.findAll();
        assertThat(infoList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Info.class);
        Info info1 = new Info();
        info1.setId(1L);
        Info info2 = new Info();
        info2.setId(info1.getId());
        assertThat(info1).isEqualTo(info2);
        info2.setId(2L);
        assertThat(info1).isNotEqualTo(info2);
        info1.setId(null);
        assertThat(info1).isNotEqualTo(info2);
    }
}
