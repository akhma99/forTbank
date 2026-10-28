package academy.backend.market_pulse.repository.store;

import academy.backend.market_pulse.repository.store.impl.QuoteFileStoreSkeleton;

class QuoteFileStoreSkeletonTest extends QuoteFileStoreContractTest {

    @Override
    protected QuoteFileStore createStore() {
        return new QuoteFileStoreSkeleton();
    }
}
