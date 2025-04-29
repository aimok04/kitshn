module Fastlane
  module Actions
    class UpdateChangelogAction < Action
      def self.run(params)
        commits = `git log --oneline`
          .split("\n")
          .map { |string| "-" + string[7..-1] }
          
        commit_feat = []
        commit_fix = []
        commit_chore = []
        commit_other = []
          
        for commit in commits do
          if commit.start_with?("- release") then
            break
          end
  
          if commit.start_with?("- feat") then
            commit_feat.push(commit)
          elsif commit.start_with?("- fix") then
            commit_fix.push(commit)
          elsif commit.start_with?("- chore") then
            commit_chore.push(commit)
          else
            commit_other.push(commit)
          end
        end
        
        new_commits = [commit_feat, commit_fix, commit_chore, commit_other].reduce([], :concat)
      
        changelog = File.read("fastlane/metadata/changelog.md")
          .sub(/\n## Highlights\n\n(.|\n|\t|\r)+/, "\n## Highlights\n")
          
        File.write(
          "fastlane/metadata/changelog.md", 
          changelog + "\n" + new_commits.join("\n")
        )
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        'Update changelog.md'
      end

      def self.is_supported?(platform)
        true
      end
    end
  end
end
