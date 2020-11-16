package edu.buaa.service;

import edu.buaa.domain.Constants;
import edu.buaa.domain.Info;
import edu.buaa.repository.InfoRepository;
import edu.buaa.web.rest.util.ImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing Info.
 */
@Service
@Transactional
public class InfoService {

    private final Logger log = LoggerFactory.getLogger(InfoService.class);

    private final InfoRepository infoRepository;

    public InfoService(InfoRepository infoRepository) {
        this.infoRepository = infoRepository;
    }

    /**
     * Save a info.
     *
     * @param info the entity to save
     * @return the persisted entity
     */
    public Info save(Info info) {
        log.debug("Request to save Info : {}", info);
        return infoRepository.save(info);
    }

    /**
     * Get all the infos.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<Info> findAll(Pageable pageable) {
        log.debug("Request to get all Infos");
        return infoRepository.findAll(pageable);
    }


    /**
     * Get one info by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public Optional<Info> findOne(Long id) {
        log.debug("Request to get Info : {}", id);
        return infoRepository.findById(id);
    }

    /**
     * Delete the info by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Info : {}", id);
        infoRepository.deleteById(id);
    }

    public void deleteall() {
        log.debug("Request to delete all Info ");
        infoRepository.deleteAll();
    }

    @Transactional(readOnly = true)
    public List<Info> findAllInfo() {
        log.debug("Request to get all Infos");
        return infoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public boolean existsbyname(String name) {
        return infoRepository.existsByfileName(name);
    }

    public void saveIMG(Info info) {
        String targetPath = Constants.filepathtosaveimg + File.separator + Constants.Edgename + File.separator + info.getFile_name() + ".png";
        byte[] bytes = info.getFile_body();
        InputStream input = new ByteArrayInputStream(bytes);
        ImageUtil.readBin2Image(input, targetPath);
    }
}
