import { RecoilRoot } from 'recoil';
import Header from './components/Header';
import MapArea from './components/MapArea';

import './App.css';
import React from 'react';

function App() {
  return (
    <div className="App">
      <RecoilRoot>
        <React.Suspense fallback={<div>Loading...</div>}>
          <Header />
          <MapArea />
        </React.Suspense>
      </RecoilRoot>
    </div>
  );
}

export default App;
