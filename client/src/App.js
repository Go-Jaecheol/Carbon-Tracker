import React from 'react';
import { RecoilRoot } from 'recoil';

import Header from './components/Header';
import Loading from './components/Loading';
import MapArea from './components/MapArea';

function App() {
  return (
    <div className="App">
      <RecoilRoot>
        <React.Suspense fallback={<Loading />}>
          <Header />
          <MapArea />
        </React.Suspense>
      </RecoilRoot>
    </div>
  );
}

export default App;