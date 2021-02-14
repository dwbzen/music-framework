import json
import pandas as pd
import numpy as np

class Song:
  """Represents a JSON format song"""

  def __init__(self, raw_data={}, json_file_name=''):
    self.song_notes = []
    self.song_harmony = []
    self.section_notes = {}   # key is section name
    self.section_harmony = {} # key is section name
    self.section_names = []
    self.data = {}
    self.song_name = 'None'
    if (len(json_file_name) >0):
      _song_file = json_file_name
      with open(_song_file, "r") as read_file:
        self.data = json.load(read_file)
    else:
      self.data = raw_data
    self._get_song_data()

  def _get_song_data(self):
    if('name' in self.data):
      self.song_name = self.data['name']
    for section in self.data['sections']:
      section_name = section['name']
      self.section_names.append(section_name)
      print('getting section name: {}'.format(section_name))  
      measures = section['measures']
      self.section_notes[section_name] = []
      self.section_harmony[section_name] = []
      for measure in measures:
        measure_number = measure['number']
        if('timeSignature' in measure):
          self.time_signature = measure['timeSignature']
        if('melody' in measure):
          melody = measure['melody']
          notes = melody['notes']
          if(len(notes) > 0):
            for n in notes:
              n['section']=section_name
              n['measure'] = measure_number
              self.song_notes.append(n)
              self.section_notes[section_name].append(n)
        if('harmony' in measure):
          harmony = measure['harmony']
          if(len(harmony) > 0):
            for h in harmony:
              h['section'] = section_name
              h['measure'] = measure_number
              self.section_harmony[section_name].append(h)
              self.song_harmony.append(h)
    """
    Create data frames for song_notes and song_harmony
    """
    self.df_notes = pd.DataFrame(self.song_notes)
    self.df_notes = self.df_notes.fillna(value={'chord':'0'})
    self.df_harmony = pd.DataFrame(self.song_harmony)
    self.df_harmony = self.df_harmony.fillna(value={'chord':'0'})

  def __iter__(self):
    """ Iterate over the song_notes """
    return self.song_notes.__iter__()
	