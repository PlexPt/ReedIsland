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

package sh.xsl.reedisland.data.local.dao

//import androidx.room.Dao
//import androidx.room.Insert
//import androidx.room.OnConflictStrategy
//import androidx.room.Query
//import sh.xsl.reedisland.data.local.entity.LuweiNotice
//
//@Dao
//interface LuweiNoticeDao {
//    @Query("SELECT * From LuweiNotice ORDER BY id DESC LIMIT 1")
//    suspend fun getLatestLuweiNotice(): LuweiNotice?
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertNotice(luweiNotice: LuweiNotice)
//
//    suspend fun insertNoticeWithTimestamp(luweiNotice: LuweiNotice) {
//        luweiNotice.setUpdatedTimestamp()
//        insertNotice(luweiNotice)
//    }
//
//    @Query("DELETE FROM LuweiNotice")
//    suspend fun nukeTable()
//}