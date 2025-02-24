package cit.edu.mmr.service;


import cit.edu.mmr.repository.TimeCapsuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TimeCapsuleService {

    @Autowired
    private TimeCapsuleRepository tcrepo;

    public TimeCapsuleService(){
        super();
    }

    
}
