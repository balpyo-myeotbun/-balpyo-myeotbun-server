package site.balpyo.script.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import site.balpyo.script.entity.ETag;
import site.balpyo.script.entity.Tag;
import site.balpyo.script.repository.TagRepository;

@Component
public class TagDataLoader implements CommandLineRunner {

    @Autowired
    TagRepository tagRepository;

    @Override
    public void run(String... args) throws Exception {
        for (ETag eTag : ETag.values()) {
            if (tagRepository.findByTag(eTag).isEmpty()) {
                tagRepository.save(new Tag(eTag));
            }
        }
    }
}
