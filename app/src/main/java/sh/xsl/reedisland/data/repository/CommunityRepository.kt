/*
 *  Copyright 2020 Fishballzzz
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package sh.xsl.reedisland.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import sh.xsl.reedisland.data.local.dao.CommunityDao
import sh.xsl.reedisland.data.local.dao.TimelineDao
import sh.xsl.reedisland.data.local.entity.Community
import sh.xsl.reedisland.data.remote.APIDataResponse
import sh.xsl.reedisland.data.remote.NMBServiceClient
import sh.xsl.reedisland.util.DataResource
import sh.xsl.reedisland.util.LoadingStatus
import sh.xsl.reedisland.util.getLocalListDataResource
import sh.xsl.reedisland.util.getLocalLiveDataAndRemoteResponse
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommunityRepository @Inject constructor(
    private val webService: NMBServiceClient,
    private val communityDao: CommunityDao,
    private val timelineDao: TimelineDao
) {
    val communityList = getLiveData(communityDao::getAll, webService::getCommunities)

    private inline fun <reified T> getLiveData(
        noinline localFetcher: () -> LiveData<List<T>>,
        noinline remoteFetcher: suspend () -> APIDataResponse<List<T>>
    ): LiveData<DataResource<List<T>>> {
        Timber.d("Getting Live ${T::class.simpleName}")
        val cache = getLocalDataSource(localFetcher)
        val remote = getServerDataSource(remoteFetcher)
        return getLocalLiveDataAndRemoteResponse(cache, remote)
    }

    private inline fun <reified T> getLocalDataSource(localFetcher: () -> LiveData<List<T>>): LiveData<DataResource<List<T>>> {
        Timber.d("Querying local ${T::class.simpleName}")
        return getLocalListDataResource(localFetcher())
    }

    private inline fun <reified T> getServerDataSource(noinline remoteFetcher: suspend () -> APIDataResponse<List<T>>): LiveData<DataResource<List<T>>> {
        return liveData {
            Timber.d("Querying remote ${T::class.simpleName}")
            val response = DataResource.create(remoteFetcher())
            if (response.status == LoadingStatus.ERROR) {
                response.message = "无法读取板块列表...\n${response.message}"
            }
            if (response.status == LoadingStatus.SUCCESS) {
                emit(response)
                updateCache(response.data!!, false)
            }
        }
    }

    private suspend inline fun <reified T> updateCache(remote: List<T>, remoteDataOnly: Boolean) {
        val comparator = when (T::class) {
            Community::class -> communityList.value?.data
            else -> throw Exception("Type not recognized")
        }
        if (remote.isNotEmpty() && (remoteDataOnly || remote != comparator)) {
            Timber.d("Remote ${T::class} differs from local or forced refresh. Updating...")
            @Suppress("UNCHECKED_CAST")
            when (T::class) {
                Community::class -> {
                    communityDao.insertAll(remote as List<Community>)
                    timelineDao.insertAll(remote.first { it.isTimeLine() }.forums.map { it.toTimeLine() })
                }
                else -> throw Exception("Type not recognized")
            }

        }
    }

    suspend fun saveCommonCommunity(community: Community) {
        communityDao.insert(community)
    }

}