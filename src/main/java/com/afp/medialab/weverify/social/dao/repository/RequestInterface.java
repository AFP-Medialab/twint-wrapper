package com.afp.medialab.weverify.social.dao.repository;

import com.afp.medialab.weverify.social.dao.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.SortedSet;

public interface RequestInterface extends JpaRepository<Request, Integer> {
    Request findByKeywordsAndBannedWordsAndLanguageAndSinceAndUntil(SortedSet<String> and_list, SortedSet<String> or_list, String lang, Date from, Date until);
}
