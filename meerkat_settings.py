
'''
renames the contracted column name in rrd to original name  when showing on web.
following should be copied into meerkat's settings.py

FORMAT: type_db_key:{'original_column_name':'renamed_column_name'}
@where
type_db_key: rrdfilename(without ext) and also matching type.db key name
original_column_name: one of column name under key in type.db
renamed_column_name: to be renamed.

'''
HUMAN_READABLE_COLUMN_NAME_MAPPING = {
    'cpu':{ 'wait':'iowait', },
    'cpu_total':{ 'wait':'iowait', },
    'cpu_consolidated':{ 'wait':'iowait', },
    'dfs_datanode':{ 
        '_blkChksumOpAvgTm':'blockChecksumOp_avg_time',
        '_blkChksumOpNumOp':'blockChecksumOp_num_ops',
        '_blkRprtAvgTm':'blockReports_avg_time', 
        '_blkRprtNumOps':'blockReports_num_ops',
        '_blkVerifiFail':'block_verification_failures',
        '_cpyBlkOpAvgTm':'copyBlockOp_avg_time',
        '_cpyBlkOpNumOps':'copyBlockOp_num_ops',
        '_heartBeatAvgTm':'heartBeats_avg_time',
        '_heartBeatsNumOps':'heartBeats_num_ops',
        '_readBlkOpAvgTm':'readBlockOp_avg_time',
        '_readBlkOpNumOps':'readBlockOp_num_ops',
        '_readsFrmLclCli':'reads_from_local_client',
        '_readFrmRmtCli':'reads_from_remote_client',
        '_rplcBlkOpAvgTm':'replaceBlockOp_avg_time',
        '_rplcBlkOpNumOps':'replaceBlockOp_num_ops',
        '_writeBlkOpAvgTm':'writeBlockOp_avg_time',
        '_writeBlkOpNumOps':'writeBlockOp_num_ops',
        '_writesFrmLclCli':'writes_from_local_client',
        '_writesFrmRmtCli':'writes_from_remote_client',
       },
                                      
    'dfs_FSNamesystem':{
        '_CapacityRemainGB':'CapacityRemainingGB',
        '_PendingDelBlks':'PendingDeletionBlocks',
        '_PendingRepliBlks':'PendingReplicationBlocks',
        '_SchedulRepliBlks':'ScheduledReplicationBlocks',
        '_UnderReplicaBlks':'UnderReplicatedBlocks',
        },
    'dfs_namenode':{
        #'_FilesInGetListOps':'FilesInGetListingOps',
        #'_JourlTXBatchInSync':'JournalTransactionsBatchedInSync',
        '_TX_avg_time':'Transactions_avg_time',
        '_TX_num_ops':'Transactions_num_ops',
        '_blkReportAvgTime':'blockReport_avg_time',
        '_blkReportNumOps':'blockReport_num_ops',
         },
    'jvm_metrics':{
        '_memNonHeapCmit':'memNonHeapCommittedM',
        '_threadsTimedWait':'threadsTimedWaiting',
        },


    'mapred_jobtracker':{
        #'_occupiedReduceSlot':'occupied_reduce_slots',
        #'_reservedMapSlots':'reserved_map_slots',
        #'_reservedReduceSlot':'reserved_reduce_slots',
        #'_trackersBlacklist':'trackers_blacklisted',
        #'_trackersDecommiss':'trackers_decommissioned',
     },
     'mapred_shuffleInput':{
        '_shflFailedFetch':'shuffle_failed_fetches',
        '_shflFtchrBsyPcnt':'shuffle_fetchers_busy_percent',
        '_shflInputBytes':'shuffle_input_bytes',
        '_shflSuccessFtchs':'shuffle_success_fetches',
        },
    'mapred_shuffleOutput':{
        '_shflFailedOutput':'shuffle_failed_outputs',
        '_shflHndlrBsyPcnt':'shuffle_handler_busy_percent',
        '_shflOutputBytes':'shuffle_output_bytes',
        '_shflSuccOutput': 'shuffle_success_outputs',
    },
    'mapred_tasktracker':{
        '_tasksFailTimeout':'tasks_failed_timeout',
    },
}
