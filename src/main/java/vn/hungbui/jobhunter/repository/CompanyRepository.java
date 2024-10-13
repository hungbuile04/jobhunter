package vn.hungbui.jobhunter.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import vn.hungbui.jobhunter.domain.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company,Long>, JpaSpecificationExecutor<Company>{
    Company save(Company company);
}
