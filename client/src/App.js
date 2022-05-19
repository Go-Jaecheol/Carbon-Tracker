import { RecoilRoot } from 'recoil';
import Header from './components/Header';
import MapArea from './components/MapArea';

import './App.css';

function App() {
  return (
    <div className="App">
      <RecoilRoot>
        <Header />
        <MapArea />
      </RecoilRoot>
    </div>
  );
}

export default App;
