
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
       },
    'dfs_FSNamesystem':{
        '_CapacityRemainGB':'CapacityRemainingGB',
        '_PendingRepliBlks':'PendingReplicationBlocks',
        '_ScheduledRepliBlks':'ScheduledReplicationBlocks',
        '_UnderReplicaBlks':'UnderReplicatedBlocks',
        },
    'dfs_namenode':{
        #'_FilesInGetListOps':'FilesInGetListingOps',
        #'_JourlTXBatchInSync':'JournalTransactionsBatchedInSync',
        '_TX_avg_time':'Transactions_avg_time',
        '_TX_num_ops':'Transactions_num_ops',
        '_blkReport_avg_time':'blockReport_avg_time',
        '_blkReport_num_ops':'blockReport_num_ops',
         },
    'jvm_metrics':{
        '_memNonHeapCommitM':'memNonHeapCommittedM',
        #'threadsTimedWaiting':'threadsTimedWaiting',
        },

    'mapred_jobtracker':{
        #'_occupiedReduceSlot':'occupied_reduce_slots',
        #'_reservedMapSlots':'reserved_map_slots',
        #'_reservedReduceSlot':'reserved_reduce_slots',
        #'_trackersBlacklist':'trackers_blacklisted',
        #'_trackersDecommiss':'trackers_decommissioned',
     },
     'mapred_shuffleInput':{
        '_shuflFailedFetch':'shuffle_failed_fetches',
        '_shuflFtchrBsyPcnt':'shuffle_fetchers_busy_percent',
        '_shuflSuccessFtchs':'shuffle_success_fetches',
        },
    'mapred_shuffleOutput':{
        '_shuflFailedOutput':'shuffle_failed_outputs',
        '_shuflHndlrBsyPcnt':'shuffle_handler_busy_percent',
        '_shuflOutputBytes':'shuffle_output_bytes',
        '_shuflSuccOutput': 'shuffle_success_outputs',
    },
    'mapred_tasktracker':{
        '_tasksFailedTimeout':'tasks_failed_timeout',
    },
}
