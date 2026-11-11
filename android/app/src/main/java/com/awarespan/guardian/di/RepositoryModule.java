package com.awarespan.guardian.di;

import com.awarespan.guardian.data.repository.AuthRepositoryImpl;
import com.awarespan.guardian.data.repository.AutopilotRepositoryImpl;
import com.awarespan.guardian.data.repository.DatasetRepositoryImpl;
import com.awarespan.guardian.data.repository.EvidenceRepositoryImpl;
import com.awarespan.guardian.data.repository.IncidentRepositoryImpl;
import com.awarespan.guardian.data.repository.NodeRepositoryImpl;
import com.awarespan.guardian.domain.repository.AuthRepository;
import com.awarespan.guardian.domain.repository.AutopilotRepository;
import com.awarespan.guardian.domain.repository.DatasetRepository;
import com.awarespan.guardian.domain.repository.EvidenceRepository;
import com.awarespan.guardian.domain.repository.IncidentRepository;
import com.awarespan.guardian.domain.repository.NodeRepository;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public abstract class RepositoryModule {

    @Binds
    abstract IncidentRepository bindIncidentRepository(IncidentRepositoryImpl impl);

    @Binds
    abstract NodeRepository bindNodeRepository(NodeRepositoryImpl impl);

    @Binds
    abstract AutopilotRepository bindAutopilotRepository(AutopilotRepositoryImpl impl);

    @Binds
    abstract EvidenceRepository bindEvidenceRepository(EvidenceRepositoryImpl impl);

    @Binds
    abstract AuthRepository bindAuthRepository(AuthRepositoryImpl impl);

    @Binds
    abstract DatasetRepository bindDatasetRepository(DatasetRepositoryImpl impl);
}
