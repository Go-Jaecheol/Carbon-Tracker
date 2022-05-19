import './App.css';
import EnergyChart from './components/EnergyChart';
import Header from './components/Header';
import MapArea from './components/MapArea';
import Modal from './components/Modal';

function App() {
  return (
    <div className="App">
      <Header />
      <Modal />
      <MapArea />
    </div>
  );
}

export default App;
