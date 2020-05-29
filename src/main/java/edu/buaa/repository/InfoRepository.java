package edu.buaa.repository;

import edu.buaa.domain.Info;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the Info entity.
 */
@SuppressWarnings("unused")
@Repository
public interface InfoRepository extends JpaRepository<Info, Long>, JpaSpecificationExecutor<Info> {

}