package com.favorites.repository;

/**
 * @Description: 获取收藏夹
 **/
public interface CollectorRepository {

    Long getMostCollectUser();

    Long getMostFollowedUser(Long notUserId);

    Long getMostPraisedUser(String notUserIds);

    Long getMostCommentedUser(String notUserIds);

    Long getMostPopularUser(String notUserIds);

    Long getMostActiveUser(String notUserIds);

}
